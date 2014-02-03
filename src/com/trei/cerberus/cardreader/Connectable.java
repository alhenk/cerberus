package com.trei.cerberus.cardreader;
/**
 * Interface RFID CR95HF Connectable
 * @author alhen
 * @date November, 25 2013
 */
interface Connectable {
	
	byte HID_REPORT_TYPE_BYTE = 0;
	byte CMD_BYTE = 1;
	byte LENGTH_BYTE = 2;
	byte DATA_START_BYTE = 3;
	/*Supplemet*/
    int ZERO = 0;
	int  RESPONSE_TIMEOUT = 3;
	byte MCU_HID_REPORT_FILLER  = (byte)0xCC;
	/*Request report commands*/
	byte MCU_HID_REPORT  =  (byte)0x01;
	byte CR95HF_HID_REPORT  = (byte)0x07;
	byte ECHO  =  (byte)0x55;
	byte SELECT_ISO  =  (byte)0x02; 
	byte ISO14443A   =  (byte)0x02;
	byte ISO15693    =  (byte)0x01;
	byte SENDRECV    =  (byte)0x04;
	/*Response command description*/
	byte CMD_ECHO_OK  =  (byte)0x55;
	byte CMD_PROTOCOL_SELECT_SUCCESS  = (byte)0x00;
	byte CMD_UID_OK  =  (byte)0x80;
	byte CMD_INVALID_COMMAND_LENGTH  =  (byte)0x82;
	byte CMD_INVALID_PROTOCOL  =  (byte)0x83;
	byte CMD_COMMUNICATION_ERROR  =  (byte)0x86;
	byte CMD_NO_UID  =  (byte)0x87;
	byte CMD_INVALID_SOF  =  (byte)0x88;
	byte CMD_RECEIVE_BUFFER_OVERFLOW  =  (byte)0x89;
	byte CMD_FRAMING_ERROR  =  (byte)0x8A;
	byte CMD_RECEPTION_LOST_WITHOUT_EOF_RECEIVED  =  (byte)0x8E;
	/*Statuses*/
	int OK    = 0;
	int ERROR =-1;

}
