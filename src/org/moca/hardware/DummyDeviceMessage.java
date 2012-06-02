
package org.moca.hardware;

public class DummyDeviceMessage implements IDeviceMessage {

	public DummyDeviceMessage() {
		// TODO Auto-generated constructor stub
	}
	
	public Byte[] getByteData() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public int formatPayload(byte[] buffer, int offset, int sizeAvailable) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int command() {
		// TODO Auto-generated method stub
		return 0;
	}

}
