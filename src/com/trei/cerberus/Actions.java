package com.trei.cerberus;

import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.trei.cerberus.cardreader.CardReader;
import com.trei.cerberus.cardreader.CardReaderException;
import com.trei.cerberus.cardreader.Code;
import com.trei.cerberus.cardreader.Cr95hf;
import com.trei.cerberus.cardreader.Report;
import com.trei.cerberus.util.Timer;

public class Actions {
	final static Logger LOGGER = Logger.getLogger(Actions.class);
	
	final static GpioController gpio = GpioFactory.getInstance();
	final static GpioPinDigitalOutput doorLock = gpio.provisionDigitalOutputPin(
											RaspiPin.GPIO_10, "Relay_K2", PinState.LOW);
	final static GpioPinDigitalOutput alarm = gpio.provisionDigitalOutputPin(
											RaspiPin.GPIO_11, "Relay_K1", PinState.LOW);
	final static GpioPinDigitalInput doorCerberus = gpio.provisionDigitalInputPin(
							RaspiPin.GPIO_05,				// PIN NUMBER
							"MyButton",						// PIN FRIENDLY NAME (optional)
							PinPullResistance.PULL_UP);	// PIN RESISTANCE (optional)
	
	private static  Thread timer;
	private static  Report report;
	private static  CardReader cardReader = new Cr95hf();
	
	// 6E016AEF Baytugelov
	// 5EDA64EF Motorin
	// 5EFF68EF Gritsenko
	// 0A722630 Tsygelnyi
	static String[] Uid = { "e004010059475b970000", "3a769f18", "6E016AEF",
							"5EDA64EF", "5EFF68EF", "0A722630" };
	
	
	public static void run() {
		timer = new Timer();
		timer.start();
		alarm.high();
		try{
			while(true){
				try {
					cycle();
				}catch (ExecutionException e) {
					LOGGER.error("Failed to execute.");
				}
			}
		}catch(Exception e){
			LOGGER.error("General Exception");
		}finally {
			try {
				cardReader.close();
			} catch (CardReaderException e) {
				LOGGER.error("Failed to close cardReader.");
			}
			doorLock.low();
			alarm.low();
			gpio.shutdown();
			LOGGER.info("Application has been closed");
		}
	}
	
	private static void cycle() throws ExecutionException{
		try {
			cardReader.openById();
			LOGGER.info("Card Reader was opened.");
		} catch (CardReaderException e) {
			LOGGER.error("Failed to open Card Reader.");
			throw new ExecutionException();
		}
		
		while(true){	
			try{
				if (isDoorOpen() && isTimerDown()){
					raiseAlarm();
				} else {
					shutdownAlarm();
				}
				report = cardReader.getUID();
				if (isUidValid(report)){
					unlockDoor();
					restartTimer();
				}	
			}catch(CardReaderException e) {
				LOGGER.error("Failed to connect.");
				throw new ExecutionException();
			}
		}
	}
	
	private static boolean isUidValid(Report report){
		
		if ((report.getStatus() == 0)
				&& (!report.getData().equals("no_uid"))) {
				if (report.getCode() == Code.UID_OK) {
					LOGGER.info("UID = " + report.getData().toUpperCase());
				}
				for (int i = 0; i < Uid.length; i++) {
					if ((Uid[i].toUpperCase()).equals(report.getData().toUpperCase())) {
						return true;
					}
				}
			}
		return false;
	}
	
	private static void unlockDoor(){
		doorLock.pulse(1000, true);
		return;
	}
	
	private static boolean isDoorOpen(){
		return doorCerberus.isHigh();
	}
	
	private static boolean isTimerDown(){
		return !((Timer) timer).isActive();
	}
	private static void restartTimer(){
		((Timer) timer).setRestart(true);
		return;
	}
	
	private static void raiseAlarm(){
		alarm.high();
		return;
	}
	private static void shutdownAlarm(){
		alarm.low();;
		return;
	}

}
