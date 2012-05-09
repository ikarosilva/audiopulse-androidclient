package org.moca.hardware;

public interface IDeviceMessage {
	int command();
	String getType();
	int formatPayload(byte[] buffer, int offset, int size_available);
}
