package com.trei.cerberus.cardreader;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDManager;

/**
 * Connecting to RFID reader STM RF transceiver board on CR95HF
 * 
 * @author alhen
 * @date November, 25 2013
 */
public class Cr95hf extends CardReader implements Connectable {
	private static final Logger LOGGER = Logger.getLogger(Cr95hf.class);
	private static final long READ_UPDATE_DELAY_MS = 50L;
	private static final int DEFAULT_VENDOR_ID = 0x0483;
	private static final int DEFAULT_PRODUCT_ID = 0xd0d0;
	private static final int DEFAULT_HID_REPORT_SIZE = 64;

	static {
		System.loadLibrary("hidapi-jni");
	}

	private int hidReportSize;	// HID report descriptor size
	private int vendorId;		// STM RF transceiver board vendor ID = 0x0483;
	private int productId;		// STM RF transceiver board product ID = 0xd0d0;
	private HIDDevice device;
	private HIDManager hidManager;

	private byte[] request;
	private byte[] response;
	private HIDReport report;

	/**
	 * Default constructor
	 */
	public Cr95hf() {
		this(DEFAULT_VENDOR_ID, DEFAULT_PRODUCT_ID, DEFAULT_HID_REPORT_SIZE);
	}

	/**
	 * Constructor with parameters
	 * @param vendorId		STM RF transceiver board vendor		ID = 0x0483
	 * @param productId		STM RF transceiver board product	ID = 0xd0d0
	 * @param hidReportSize	64 bytes
	 */
	public Cr95hf(int vendorId, int productId, int hidReportSize) {
		this.vendorId = vendorId;
		this.productId = productId;
		this.hidReportSize = hidReportSize;
		report = new HIDReport();
		request = new byte[hidReportSize];
		response = new byte[hidReportSize];
	}

	/**
	 * Open HID device by ID
	 * @param vendorId		STM RF transceiver board vendor		ID = 0x0483
	 * @param productId		STM RF transceiver board product	ID = 0xd0d0
	 * @return device		HIDDevice instance
	 */
	private HIDDevice openById(int vendorId, int productId)
			throws CardReaderException {
		try {
			hidManager = HIDManager.getInstance();
			device = hidManager.openById(vendorId, productId, null);
			//LOGGER.info("Manufacturer: " + device.getManufacturerString());
			//LOGGER.info("Product: " + device.getProductString());
			//LOGGER.info("Serial Number: " + device.getSerialNumberString());
		} catch (IOException e) {
			LOGGER.warn(e);
			throw new CardReaderException();
		}
		return device;
	}// openById

	public Object openById() throws CardReaderException {
		return openById(vendorId, productId);
	}// openById

	/**
	 * Send request and receive response
	 * @return status
	 * @throws CardReaderException
	 */
	private int sendReceive() throws CardReaderException {
		int status = ERROR;
		try {
			device.disableBlocking(); // Unblocking reading - very crucial!
			device.write(request);
			int n = 0;
			int timeOutCounter = 100;
			while (n == 0) {
				n = device.read(response);
				if (timeOutCounter-- <= 0){
					LOGGER.warn("CR95hf reading TimeOut");
					throw new CardReaderException();
				};
				if (n > 0){
					status = OK;
				}
				try {
					Thread.sleep(READ_UPDATE_DELAY_MS);
				} catch (InterruptedException e) {
					// Ignore
					e.printStackTrace();
				}
			}// while
		} catch (IOException e) {
			LOGGER.warn(e);
			throw new CardReaderException();
		}// try
		return status;
	}// SendReceive

	/**
	 * Send echo (value 0x55)
	 * @return status
	 * @throws CardReaderException 
	 */
	public int echo() throws CardReaderException {
		int status = OK;
		int responseTimeoutCounter = RESPONSE_TIMEOUT;
		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;
		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = ECHO;
		while (response[CMD_BYTE] != CMD_ECHO_OK) {
			sendReceive();
			if (responseTimeoutCounter-- <= 0) {
				report.setCode(Code.ECHO_NO_ANSWER);
				status = ERROR;
				break;
			}
		}
		return status;
	}// echo

	/**
	 * Select RFID protocol: ISO15693 or ISO14443A
	 * @param protocol
	 * @return status
	 * @throws CardReaderException 
	 */
	private int selectProtocol(int protocol) throws CardReaderException {
		int status = OK;
		int dataIdx = DATA_START_BYTE;

		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;

		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = SELECT_ISO;
		request[LENGTH_BYTE] = (byte) 0x02;
		switch (protocol) {
		case ISO14443A:
			request[dataIdx++] = ISO14443A;
			request[dataIdx++] = ZERO;
			break;

		case ISO15693:
			request[dataIdx++] = ISO15693;
			request[dataIdx++] = (byte) 0x0D;
			break;
		}
		for (int i = 0; i < 3; i++)
			request[dataIdx++] = ZERO; // CRC OK bytes

		sendReceive();
		if (response[CMD_BYTE] != CMD_PROTOCOL_SELECT_SUCCESS) {
			report.setData(byteToHex(response, hidReportSize));
			report.setCode(getErrorCode(response[CMD_BYTE]));
			status = ERROR;
		}

		return status;
	}// selectProtocol

	/**
	 * RFID request ISO14443A
	 * @throws CardReaderException 
	 */
	private int request14443A() throws CardReaderException {

		byte[] hexData = new byte[hidReportSize];
		int status = 0;
		int hexDataSize = 0;
		int dataIdx = DATA_START_BYTE;

		status = selectProtocol(ISO14443A);
		/*--SENDRECV REQA--------------------------------------*/
		dataIdx = DATA_START_BYTE;
		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;
		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = SENDRECV;
		request[LENGTH_BYTE] = (byte) 0x02;
		request[dataIdx++] = (byte) 0x26;
		request[dataIdx++] = (byte) 0x07;
		for (int i = 0; i < 3; i++)
			request[dataIdx++] = ZERO;
		sendReceive();
		/*--END OF SENDRECV REQA--------------------------------*/

		/*--SENDRECV ANTICOL CL1--------------------------------*/
		dataIdx = DATA_START_BYTE;
		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;
		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = SENDRECV;
		request[LENGTH_BYTE] = (byte) 0x03;
		request[dataIdx++] = (byte) 0x93;
		request[dataIdx++] = (byte) 0x20;
		request[dataIdx++] = (byte) 0x08;
		for (int i = 0; i < 3; i++)
			request[dataIdx++] = ZERO;
		sendReceive();

		/*--END OF SENDRECV ANTICOL CL1-------------------------*/

		if (status == OK) {
			/*--getting hexadecimal data/uid-------------------------*/
			hexDataSize = ((int) response[2] != 0) ? ((int) response[2] - 4)
					: 1;
			for (int i = 0, j = DATA_START_BYTE; i < hexDataSize; i++, j++)
				hexData[i] = response[j];
			if (response[CMD_BYTE] == CMD_UID_OK) {
				report.setData(byteToHex(hexData, hexDataSize));
				report.setCode(Code.UID_OK);
				status = OK;
			} else if (response[CMD_BYTE] == CMD_NO_UID) {
				report.setData("no_uid");
				report.setCode(Code.NO_UID);
				status = OK;
			} else {
				report.setData(byteToHex(response, hidReportSize));
				report.setCode(getErrorCode(response[CMD_BYTE]));
				status = ERROR;
			}
			/*--end of getting hexadecimal uid------------------*/
		}
		/*--SENDRECV UID ECHO-----------------------------------*/
		dataIdx = DATA_START_BYTE;
		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;
		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = SENDRECV;
		request[LENGTH_BYTE] = (byte) (hexDataSize + 3);
		request[dataIdx++] = (byte) 0x93;
		request[dataIdx++] = (byte) 0x70;
		for (int i = 0; i < hexDataSize; i++)
			request[dataIdx++] = hexData[i];
		request[dataIdx++] = (byte) 0x28;
		for (int i = 0; i < 3; i++)
			request[dataIdx++] = ZERO; // CRC OK bytes
		sendReceive();
		/*--END OF SENDRECV UID ECHO----------------------------*/

		/*--FINAL SENDRECV--------------------------------------*/
		dataIdx = DATA_START_BYTE;
		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;
		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = SENDRECV;
		request[LENGTH_BYTE] = (byte) 0x03;
		request[dataIdx++] = (byte) 0x30;
		request[dataIdx++] = (byte) 0x00;
		request[dataIdx++] = (byte) 0x28;
		for (int i = 0; i < 3; i++)
			request[dataIdx++] = ZERO; // CRC OK bytes
		sendReceive();
		/*--END OF FINAL SENDRECV-------------------------------*/
		return status;
	}// request14443A

	/**
	 * RFID request 15693
	 * @throws CardReaderException 
	 */
	private int request15693() throws CardReaderException {

		byte[] hexData = new byte[hidReportSize];
		int status = 0;
		int hexDataSize = 0;
		int dataIdx = DATA_START_BYTE;

		status = selectProtocol(ISO15693);

		/*--SENDRECV--------------------------------------------*/
		dataIdx = DATA_START_BYTE;
		for (int i = 0; i < hidReportSize; i++)
			request[i] = MCU_HID_REPORT_FILLER;
		request[HID_REPORT_TYPE_BYTE] = MCU_HID_REPORT;
		request[CMD_BYTE] = SENDRECV;
		request[LENGTH_BYTE] = (byte) 0x03;
		request[dataIdx++] = (byte) 0x26;
		request[dataIdx++] = (byte) 0x01;
		request[dataIdx++] = (byte) 0x00;
		for (int i = 0; i < 3; i++, dataIdx++)
			request[dataIdx] = ZERO;
		sendReceive();

		if (status == OK) {
			/*--getting hexadecimal data/uid-------------------------*/
			hexDataSize = ((int) response[2] != 0) ? ((int) response[2] - 3)
					: 1;
			for (int i = 0, j = DATA_START_BYTE; i < hexDataSize; i++, j++)
				hexData[i] = response[j];

			if (response[CMD_BYTE] == CMD_UID_OK) {
				hexData = reverseData(hexData, hexDataSize);

				StringBuilder sb = new StringBuilder(byteToHex(hexData,
						hexDataSize));
				report.setData(sb.toString());
				report.setCode(Code.UID_OK);
				status = OK;
			} else if (response[CMD_BYTE] == CMD_NO_UID) {
				report.setData("no_uid");
				report.setCode(Code.NO_UID);
				status = OK;
			} else {
				report.setData(byteToHex(response, hidReportSize));
				report.setCode(getErrorCode(response[CMD_BYTE]));
				status = ERROR;
			}
			/*--end of getting hexadecimal uid------------------*/
		}

		return status;
	}// request15693

	/**
	 * Read RFID tag UID
	 * @throws CardReaderException 
	 */
	public Report getUID() throws CardReaderException {
		report.setStatus(OK);
		report.setCode(Code.NO_UID);
		report.setData("initial data");
		report.setStatus(echo());
		if (report.getStatus() == OK) {
			request14443A();
		}
		if ((report.getStatus() == OK) && (report.getCode() != Code.UID_OK)) {
			report.setStatus(echo());
			if (report.getStatus() == OK) {
				request15693();
			}
		}
		return report;
	}// getUID

	/**
	 * Convert Byte Buffer into HEX ASCII
	 * @param buf byte buffer
	 * @param n buffer size
	 * @return String
	 */
	private static String byteToHex(byte[] buf, int n) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < n; i++) {
			int v = buf[i];
			if (v < 0)
				v = v + 256;
			if (v < 16)
				str.append("0");
			str.append(Integer.toHexString(v));
		}
		str.append("");
		return str.toString();
	}// byteToHex

	/**
	 * Convert byte error into enumerated Code
	 * @param errByte
	 * @return errCode
	 */
	private Code getErrorCode(byte errByte) {
		Code errCode;
		switch (errByte) {
		case CMD_INVALID_COMMAND_LENGTH:
			errCode = Code.INVALID_COMMAND_LENGTH;
			break;
		case CMD_INVALID_PROTOCOL:
			errCode = Code.INVALID_PROTOCOL;
			break;
		case CMD_COMMUNICATION_ERROR:
			errCode = Code.COMMUNICATION_ERROR;
			break;
		case CMD_INVALID_SOF:
			errCode = Code.INVALID_SOF;
			break;
		case CMD_RECEIVE_BUFFER_OVERFLOW:
			errCode = Code.RECEIVE_BUFFER_OVERFLOW;
			break;
		case CMD_FRAMING_ERROR:
			errCode = Code.FRAMING_ERROR;
			break;
		case CMD_RECEPTION_LOST_WITHOUT_EOF_RECEIVED:
			errCode = Code.RECEPTION_LOST_WITHOUT_EOF_RECEIVED;
			break;
		default:
			errCode = Code.UNKNOWN_COMMAND;
			break;
		}
		return errCode;
	}// getErrorCode

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public void setProductId(int productId) {
		this.productId = productId;
	}

	public int getVendorId() {
		return vendorId;
	}

	public int getProductId() {
		return productId;
	}

	public HIDReport getReport() {
		return report;
	}

	public void close() throws CardReaderException {
		try {
			device.close();
			hidManager.release();
			System.gc();
		} catch (IOException e) {
			LOGGER.warn(e);
			throw new CardReaderException();
		}
	}

	/**
	 * Reverse byte array
	 * @param data	- byte array
	 * @param dataLength - array size
	 * @return data - reversed byte array
	 */
	private byte[] reverseData(byte[] data, int dataLength) {
		byte[] temp = new byte[hidReportSize];
		int i, j;
		for (i = --dataLength, j = 0; i >= 0; i--, j++)
			temp[j] = data[i];
		for (i = 0; i < j; i++)
			data[i] = temp[i];

		return data;
	}
}