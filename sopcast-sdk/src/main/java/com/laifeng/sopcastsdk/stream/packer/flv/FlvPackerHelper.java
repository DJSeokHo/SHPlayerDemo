package com.laifeng.sopcastsdk.stream.packer.flv;

import com.laifeng.sopcastsdk.stream.amf.AmfMap;
import com.laifeng.sopcastsdk.stream.amf.AmfString;

import java.nio.ByteBuffer;

public class FlvPackerHelper {
    public static final int FLV_HEAD_SIZE = 9;
    public static final int VIDEO_HEADER_SIZE = 5;
    public static final int AUDIO_HEADER_SIZE = 2;
    public static final int FLV_TAG_HEADER_SIZE = 11;
    public static final int PRE_SIZE = 4;
    public static final int AUDIO_SPECIFIC_CONFIG_SIZE = 2;
    public static final int VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE = 11;

    /**
     * create flv header
     */
    public static void writeFlvHeader(ByteBuffer buffer, boolean hasVideo, boolean hasAudio) {

        byte[] signature = new byte[] {'F', 'L', 'V'};  /* always "FLV" */
        byte version = (byte) 0x01;     /* should be 1 */
        byte videoFlag = hasVideo ? (byte) 0x01 : 0x00;
        byte audioFlag = hasAudio ? (byte) 0x04 : 0x00;
        byte flags = (byte) (videoFlag | audioFlag);  /* 4, audio; 1, video; 5 audio+video.*/
        byte[] offset = new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09};  /* always 9 */

        buffer.put(signature);
        buffer.put(version);
        buffer.put(flags);
        buffer.put(offset);
    }

    public static void writeFlvTagHeader(ByteBuffer buffer, int type, int dataSize, int timestamp) {

        int sizeAndType = (dataSize & 0x00FFFFFF) | ((type & 0x1F) << 24);
        buffer.putInt(sizeAndType);
        int time = ((timestamp << 8) & 0xFFFFFF00) | ((timestamp >> 24) & 0x000000FF);
        buffer.putInt(time);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
    }

    public static byte[] writeFlvMetaData(int width, int height, int fps, int audioRate, int audioSize, boolean isStereo) {
        AmfString metaDataHeader = new AmfString("onMetaData", false);
        AmfMap amfMap = new AmfMap();
        amfMap.setProperty("width", width);
        amfMap.setProperty("height", height);
        amfMap.setProperty("framerate", fps);
        amfMap.setProperty("videocodecid", FlvVideoCodecID.AVC);
        amfMap.setProperty("audiosamplerate", audioRate);
        amfMap.setProperty("audiosamplesize", audioSize);
        if(isStereo) {
            amfMap.setProperty("stereo", true);
        } else {
            amfMap.setProperty("stereo", false);
        }
        amfMap.setProperty("audiocodecid", FlvAudio.AAC);

        int size = amfMap.getSize() + metaDataHeader.getSize();
        ByteBuffer amfBuffer = ByteBuffer.allocate(size);
        amfBuffer.put(metaDataHeader.getBytes());
        amfBuffer.put(amfMap.getBytes());
        return amfBuffer.array();
    }

    public static void writeFirstVideoTag(ByteBuffer buffer, byte[] sps, byte[] pps) {

        writeVideoHeader(buffer, FlvVideoFrameType.KeyFrame, FlvVideoCodecID.AVC, FlvVideoAVCPacketType.SequenceHeader);

        buffer.put((byte)0x01);
        buffer.put(sps[1]);
        buffer.put(sps[2]);
        buffer.put(sps[3]);
        buffer.put((byte)0xff);

        buffer.put((byte)0xe1);
        buffer.putShort((short)sps.length);
        buffer.put(sps);

        buffer.put((byte)0x01);
        buffer.putShort((short)pps.length);
        buffer.put(pps);
    }

    public static void writeVideoHeader(ByteBuffer buffer, int flvVideoFrameType, int codecID, int AVCPacketType) {
        byte first = (byte) (((flvVideoFrameType & 0x0F) << 4)| (codecID & 0x0F));
        buffer.put(first);

        buffer.put((byte) AVCPacketType);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
    }

    public static void writeH264Packet(ByteBuffer buffer, byte[] data, boolean isKeyFrame) {

        int flvVideoFrameType = FlvVideoFrameType.InterFrame;
        if(isKeyFrame) {
            flvVideoFrameType = FlvVideoFrameType.KeyFrame;
        }
        writeVideoHeader(buffer, flvVideoFrameType, FlvVideoCodecID.AVC, FlvVideoAVCPacketType.NALU);

        buffer.put(data);
    }

    public static void writeFirstAudioTag(ByteBuffer buffer, int audioRate, boolean isStereo, int audioSize) {
        byte[] audioInfo = new byte[2];
        int soundRateIndex = getAudioSimpleRateIndex(audioRate);
        int channelCount = 1;
        if(isStereo) {
            channelCount = 2;
        }
        audioInfo[0] = (byte) (0x10 | ((soundRateIndex>>1) & 0x7));
        audioInfo[1] = (byte) (((soundRateIndex & 0x1)<<7) | ((channelCount & 0xF) << 3));
        writeAudioTag(buffer, audioInfo, true, audioSize);
    }


    public static void writeAudioTag(ByteBuffer buffer, byte[] audioInfo, boolean isFirst, int audioSize) {
        writeAudioHeader(buffer, isFirst, audioSize);
        buffer.put(audioInfo);
    }

    public static void writeAudioHeader(ByteBuffer buffer, boolean isFirst, int audioSize) {
        int soundFormat = FlvAudio.AAC;

        // AAC always 3
        int soundRateIndex = 3;

        int soundSize = FlvAudioSampleSize.PCM_16;
        if(audioSize == 8) {
            soundSize = FlvAudioSampleSize.PCM_8;
        }

        // aac always stereo
        int soundType = FlvAudioSampleType.STEREO;

        int AACPacketType = FlvAudioAACPacketType.Raw;
        if(isFirst) {
            AACPacketType = FlvAudioAACPacketType.SequenceHeader;
        }

        byte[] header = new byte[2];
        header[0] = (byte)(((byte) (soundFormat & 0x0F) << 4) | ((byte) (soundRateIndex & 0x03) << 2) | ((byte) (soundSize & 0x01) << 1) | ((byte) (soundType & 0x01)));
        header[1] = (byte) AACPacketType;
        buffer.put(header);
    }

    public static int getAudioSimpleRateIndex(int audioSampleRate) {
        int simpleRateIndex;
        switch (audioSampleRate) {
            case 96000:
                simpleRateIndex = 0;
                break;
            case 88200:
                simpleRateIndex = 1;
                break;
            case 64000:
                simpleRateIndex = 2;
                break;
            case 48000:
                simpleRateIndex = 3;
                break;
            case 44100:
                simpleRateIndex = 4;
                break;
            case 32000:
                simpleRateIndex = 5;
                break;
            case 24000:
                simpleRateIndex = 6;
                break;
            case 22050:
                simpleRateIndex = 7;
                break;
            case 16000:
                simpleRateIndex = 8;
                break;
            case 12000:
                simpleRateIndex = 9;
                break;
            case 11025:
                simpleRateIndex = 10;
                break;
            case 8000:
                simpleRateIndex = 11;
                break;
            case 7350:
                simpleRateIndex = 12;
                break;
            default:
                simpleRateIndex = 15;
        }
        return simpleRateIndex;
    }

    /**
     * E.4.3.1 VIDEODATA
     * Frame Type UB [4]
     * Type of video frame. The following values are defined:
     *  1 = key frame (for AVC, a seekable frame)
     *  2 = inter frame (for AVC, a non-seekable frame)
     *  3 = disposable inter frame (H.263 only)
     *  4 = generated key frame (reserved for server use only)
     *  5 = video info/command frame
     */
    public class FlvVideoFrameType
    {
        // set to the zero to reserved, for array map.
        public final static int Reserved = 0;
        public final static int Reserved1 = 6;

        public final static int KeyFrame                     = 1;
        public final static int InterFrame                 = 2;
        public final static int DisposableInterFrame         = 3;
        public final static int GeneratedKeyFrame            = 4;
        public final static int VideoInfoFrame                = 5;
    }


    /**
     * AVCPacketType IF CodecID == 7 UI8
     * The following values are defined:
     *  0 = AVC sequence header
     *  1 = AVC NALU
     *  2 = AVC end of sequence (lower level NALU sequence ender is not required or supported)
     */
    public class FlvVideoAVCPacketType
    {
        // set to the max value to reserved, for array map.
        public final static int Reserved                    = 3;

        public final static int SequenceHeader                 = 0;
        public final static int NALU                         = 1;
        public final static int SequenceHeaderEOF             = 2;
    }

    /**
     * AACPacketType
     * The following values are defined:
     *  0 = AAC sequence header
     *  1 = AAC Raw
     */
    public class FlvAudioAACPacketType
    {
        public final static int SequenceHeader                 = 0;
        public final static int Raw                         = 1;
    }

    /**
     * E.4.1 FLV Tag, page 75
     */
    public class FlvTag
    {
        // set to the zero to reserved, for array map.
        public final static int Reserved = 0;
        // 8 = audio
        public final static int Audio = 8;
        // 9 = video
        public final static int Video = 9;
        // 18 = script data
        public final static int Script = 18;
    }

    /**
     * E.4.3.1 VIDEODATA
     * CodecID UB [4]
     * Codec Identifier. The following values are defined:
     *  2 = Sorenson H.263
     *  3 = Screen video
     *  4 = On2 VP6
     *  5 = On2 VP6 with alpha channel
     *  6 = Screen video version 2
     *  7 = AVC
     */
    public class FlvVideoCodecID
    {
        // set to the zero to reserved, for array map.
        public final static int Reserved                = 0;
        public final static int Reserved1                = 1;
        public final static int Reserved2                = 9;

        // for user to disable video, for example, use pure audio hls.
        public final static int Disabled                = 8;

        public final static int SorensonH263             = 2;
        public final static int ScreenVideo             = 3;
        public final static int On2VP6                 = 4;
        public final static int On2VP6WithAlphaChannel = 5;
        public final static int ScreenVideoVersion2     = 6;
        public final static int AVC                     = 7;
    }

    public class FlvAudio {
        public final static int LINEAR_PCM = 0;
        public final static int AD_PCM = 1;
        public final static int MP3 = 2;
        public final static int LINEAR_PCM_LE = 3;
        public final static int NELLYMOSER_16_MONO = 4;
        public final static int NELLYMOSER_8_MONO = 5;
        public final static int NELLYMOSER = 6;
        public final static int G711_A = 7;
        public final static int G711_MU = 8;
        public final static int RESERVED = 9;
        public final static int AAC = 10;
        public final static int SPEEX = 11;
        public final static int MP3_8 = 14;
        public final static int DEVICE_SPECIFIC = 15;
    }

    /**
     * the aac object type, for RTMP sequence header
     * for AudioSpecificConfig, @see aac-mp4a-format-ISO_IEC_14496-3+2001.pdf, page 33
     * for audioObjectType, @see aac-mp4a-format-ISO_IEC_14496-3+2001.pdf, page 23
     */
    public class FlvAacObjectType
    {
        public final static int Reserved = 0;

        // Table 1.1 – Audio Object Type definition
        // @see @see aac-mp4a-format-ISO_IEC_14496-3+2001.pdf, page 23
        public final static int AacMain = 1;
        public final static int AacLC = 2;
        public final static int AacSSR = 3;

        // AAC HE = LC+SBR
        public final static int AacHE = 5;
        // AAC HEv2 = LC+SBR+PS
        public final static int AacHEV2 = 29;
    }

    /**
     * the aac profile, for ADTS(HLS/TS)
     * @see "https://github.com/simple-rtmp-server/srs/issues/310"
     */
    public class FlvAacProfile
    {
        public final static int Reserved = 3;

        // @see 7.1 Profiles, aac-iso-13818-7.pdf, page 40
        public final static int Main = 0;
        public final static int LC = 1;
        public final static int SSR = 2;
    }

    /**
     * the FLV/RTMP supported audio sample rate.
     * Sampling rate. The following values are defined:
     */
    public class FlvAudioSampleRate
    {
        // set to the max value to reserved, for array map.
        public final static int Reserved                 = 15;

        public final static int R96000                     = 0;
        public final static int R88200                    = 1;
        public final static int R64000                    = 2;
        public final static int R48000                    = 3;
        public final static int R44100                    = 4;
        public final static int R32000                    = 5;
        public final static int R24000                    = 6;
        public final static int R22050                    = 7;
        public final static int R16000                    = 8;
        public final static int R12000                    = 9;
        public final static int R11025                    = 10;
        public final static int R8000                    = 11;
        public final static int R7350                    = 12;
    }

    /**
     * the FLV/RTMP supported audio sample size.
     * Sampling size. The following values are defined:
     * 0 = 8-bit samples
     * 1 = 16-bit samples
     */
    public class FlvAudioSampleSize
    {
        public final static int PCM_8                     = 0;
        public final static int PCM_16                    = 1;
    }

    /**
     * the FLV/RTMP supported audio sample type.
     * Sampling type. The following values are defined:
     * 0 = Mono sound
     * 1 = Stereo sound
     */
    public class FlvAudioSampleType
    {
        public final static int MONO                     = 0;
        public final static int STEREO                   = 1;
    }

    /**
     * the type of message to process.
     */
    public class FlvMessageType {
        public final static int FLV = 0x100;
    }

    /**
     * Table 7-1 – NAL unit type codes, syntax element categories, and NAL unit type classes
     * H.264-AVC-ISO_IEC_14496-10-2012.pdf, page 83.
     */
    public class FlvAvcNaluType
    {
        // Unspecified
        public final static int Reserved = 0;

        // Coded slice of a non-IDR picture slice_layer_without_partitioning_rbsp( )
        public final static int NonIDR = 1;
        // Coded slice data partition A slice_data_partition_a_layer_rbsp( )
        public final static int DataPartitionA = 2;
        // Coded slice data partition B slice_data_partition_b_layer_rbsp( )
        public final static int DataPartitionB = 3;
        // Coded slice data partition C slice_data_partition_c_layer_rbsp( )
        public final static int DataPartitionC = 4;
        // Coded slice of an IDR picture slice_layer_without_partitioning_rbsp( )
        public final static int IDR = 5;
        // Supplemental enhancement information (SEI) sei_rbsp( )
        public final static int SEI = 6;
        // Sequence parameter set seq_parameter_set_rbsp( )
        public final static int SPS = 7;
        // Picture parameter set pic_parameter_set_rbsp( )
        public final static int PPS = 8;
        // Access unit delimiter access_unit_delimiter_rbsp( )
        public final static int AccessUnitDelimiter = 9;
        // End of sequence end_of_seq_rbsp( )
        public final static int EOSequence = 10;
        // End of stream end_of_stream_rbsp( )
        public final static int EOStream = 11;
        // Filler data filler_data_rbsp( )
        public final static int FilterData = 12;
        // Sequence parameter set extension seq_parameter_set_extension_rbsp( )
        public final static int SPSExt = 13;
        // Prefix NAL unit prefix_nal_unit_rbsp( )
        public final static int PrefixNALU = 14;
        // Subset sequence parameter set subset_seq_parameter_set_rbsp( )
        public final static int SubsetSPS = 15;
        // Coded slice of an auxiliary coded picture without partitioning slice_layer_without_partitioning_rbsp( )
        public final static int LayerWithoutPartition = 19;
        // Coded slice extension slice_layer_extension_rbsp( )
        public final static int CodedSliceExt = 20;
    }


    /**
     * 0 = Number type  //DOUBLE(8个字节的double数据)
     * 1 = Boolean type //UI8(1个字节)
     * 2 = String type   //SCRIPTDATASTRING
     * 3 = Object type  //SCRIPTDATAOBJECT[n]
     * 4 = MovieClip type  //SCRIPTDATASTRING
     * 5 = Null type
     * 6 = Undefined type
     * 7 = Reference type  //UI16(2个字节)
     * 8 = ECMA array type  //SCRIPTDATAVARIABLE[ECMAArrayLength]
     * 10 = Strict array type  //SCRIPTDATAVARIABLE[n]
     * 11 = Date type  //SCRIPTDATADATE
     * 12 = Long string type  //SCRIPTDATALONGSTRING
     */
    public class FlvMetaValueType {
        public final static int NumberType = 0;
        public final static int BooleanType = 1;
        public final static int StringType = 2;
        public final static int ObjectType = 3;
        public final static int MovieClipType = 4;
        public final static int NullType = 5;
        public final static int UndefinedType = 6;
        public final static int ReferenceType = 7;
        public final static int ECMAArrayType = 8;
        public final static int StrictArrayType = 10;
        public final static int DateType = 11;
        public final static int LongStringType = 12;
    }
}
