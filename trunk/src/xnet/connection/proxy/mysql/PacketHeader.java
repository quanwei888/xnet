package xnet.connection.proxy.mysql;

public class PacketHeader {
	public final static int size = 4;
	/**
	 * Packet Length: The length, in bytes, of the packet that follows the
	 * Packet Header. There may be some special values in the most significant
	 * byte. Since 2**24 = MB, the maximum packet length is 16MB.
	 */
	public int packetLen;
	/**
	 * Packet Number: A serial number which can be used to ensure that all
	 * packets are present and in order. The first packet of a client query will
	 * have Packet Number = 0 Thus, when a new SQL statement starts, the packet
	 * number is re-initialised.
	 */
	public int packetNum;
}
