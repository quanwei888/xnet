package xnet.core;

import xnet.core.connection.IConnectionState;


public class ConnectionState implements IConnectionState {
	protected int value;
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
