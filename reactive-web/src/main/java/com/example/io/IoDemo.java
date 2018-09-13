package com.example.io;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Log4j2
public class IoDemo {

		public static void main(String args[]) throws IOException {
				try {
						String home = System.getProperty("user.home");
						File desktop = new File(home, "Desktop");
						File inputFile = new File(desktop, "input.txt");

						Consumer<BytesPayload> consumer = (bytes) ->
							log.info(String.format("bytes available! got %d bytes.", bytes.getLength()));

						Io io = new Io();

						log.info("---------------------------------");
						io.synchronousRead(inputFile, consumer);

						log.info("---------------------------------");
						io.asynchronousRead(inputFile, consumer);
				}
				catch (Exception e) {
						ReflectionUtils.rethrowRuntimeException(e);
				}
				System.in.read();
		}
}

