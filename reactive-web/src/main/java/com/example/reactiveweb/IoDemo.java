package com.example.reactiveweb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

interface Reader {
		void read(File file, Consumer<BytesPayload> consumer) throws IOException;
}

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
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

class Io {

		private final Synchronous synchronous = new Synchronous();

		public void synchronousRead(File f, Consumer<BytesPayload> consumer) {
				try {
						this.synchronous.read(f, consumer);
				}
				catch (IOException e) {
						ReflectionUtils.rethrowRuntimeException(e);
				}
		}

		public void asynchronousRead(File f, Consumer<BytesPayload> consumer) {
				try {
						Asynchronous asynchronous = new Asynchronous();
						asynchronous.read(f, consumer);
				}
				catch (Exception ex) {
						ReflectionUtils.rethrowRuntimeException(ex);
				}
		}
}

@Data
@AllArgsConstructor
class BytesPayload {

		private final byte[] bytes;
		private final int length;

		public static BytesPayload from(byte[] bytes, int len) {
				return new BytesPayload(bytes, len);
		}
}

@Log4j2
class Synchronous implements Reader {

		@Override
		public void read(File file, Consumer<BytesPayload> consumer) throws IOException {
				try (FileInputStream in = new FileInputStream(file)) {
						byte[] data = new byte[FileCopyUtils.BUFFER_SIZE];
						int res;
						while ((res = in.read(data, 0, data.length)) != -1) {
								consumer.accept(BytesPayload.from(data, res));
						}
				}
		}
}

@Log4j2
class Asynchronous implements Reader, CompletionHandler<Integer, ByteBuffer> {

		private int bytesRead;
		private long position;
		private AsynchronousFileChannel fileChannel;
		private Consumer<BytesPayload> consumer;

		@Override
		public void failed(Throwable exc, ByteBuffer attachment) {
				log.error(exc);
		}

		@Override
		public void completed(Integer result, ByteBuffer buffer) {

				this.bytesRead = result;

				if (this.bytesRead < 0)
						return;

				buffer.flip();

				byte[] data = new byte[buffer.limit()];
				buffer.get(data);

				consumer.accept(BytesPayload.from(data, data.length));
				buffer.clear();
				this.position = this.position + this.bytesRead;
				this.fileChannel.read(buffer, this.position, buffer, this);
		}

		public void read(File file, Consumer<BytesPayload> c) throws IOException {
				this.consumer = c;
				Path path = file.toPath();
				this.fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
				ByteBuffer buffer = ByteBuffer.allocate(FileCopyUtils.BUFFER_SIZE);
				this.fileChannel.read(buffer, position, buffer, this);
				while (this.bytesRead > 0) {
						this.position = this.position + this.bytesRead;
						this.fileChannel.read(buffer, this.position, buffer, this);
				}
		}
}