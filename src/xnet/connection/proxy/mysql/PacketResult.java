package xnet.connection.proxy.mysql;

/**
 * VERSION 4.0 Bytes Name ----- ---- 1 (Length Coded Binary) field_count, always
 * = 0 1-9 (Length Coded Binary) affected_rows 1-9 (Length Coded Binary)
 * insert_id 2 server_status n (until end of packet) message
 * 
 * VERSION 4.1 Bytes Name ----- ---- 1 (Length Coded Binary) field_count, always
 * = 0 1-9 (Length Coded Binary) affected_rows 1-9 (Length Coded Binary)
 * insert_id 2 server_status 2 warning_count n (until end of packet) message
 * 
 * field_count: always = 0
 * 
 * affected_rows: = number of rows affected by INSERT/UPDATE/DELETE
 * 
 * insert_id: If the statement generated any AUTO_INCREMENT number, it is
 * returned here. Otherwise this field contains 0. Note: when using for example
 * a multiple row INSERT the insert_id will be from the first row inserted, not
 * from last.
 * 
 * server_status: = The client can use this to check if the command was inside a
 * transaction.
 * 
 * warning_count: number of warnings
 * 
 * message: For example, after a multi-line INSERT, message might be
 * "Records: 3 Duplicates: 0 Warnings: 0"
 * 
 */
public class PacketResult {
	public final static int TYPE_OK = 0x00;
	public final static int TYPE_ERR = 0xFF;
	public final static int TYPE_SET = 0x00;

	public byte type;

}
