package org.devsmart.kontex;

import java.io.IOException;

import org.devsmart.kontex.bencode.BEValue;

public interface BEncodable {

	public BEValue encode() throws IOException;
	public void decode(BEValue value) throws IOException;
}
