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
		void read(File file) throws IOException;
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

						log.info("--------------------------------------------------------------");
						Reader synchronous = new Synchronous(consumer);
						synchronous.read(inputFile);

						log.info("--------------------------------------------------------------");
						Reader asynchronous = new Asynchronous(consumer);
						asynchronous.read(inputFile);
				}
				catch (Exception e) {
						ReflectionUtils.rethrowRuntimeException(e);
				}
				System.in.read();
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

		private final Consumer<BytesPayload> consumer;

		Synchronous(Consumer<BytesPayload> consumer) {
				this.consumer = consumer;
		}

		public void read(File file) throws IOException {
				try (FileInputStream in = new FileInputStream(file)) {
						byte[] data = new byte[FileCopyUtils.BUFFER_SIZE];
						int res;
						while ((res = in.read(data, 0, data.length)) != -1) {
								this.consumer.accept(BytesPayload.from(data, res));
						}
				}
		}
}

class Asynchronous implements Reader {

		private final Consumer<BytesPayload> consumer;
		private int bytesRead = 0;
		private long position = 0;
		private AsynchronousFileChannel fileChannel = null;
		private final CompletionHandler<Integer, ByteBuffer> completionHandler =
			new CompletionHandler<Integer, ByteBuffer>() {

					@Override
					public void failed(Throwable exc, ByteBuffer attachment) {
					}

					@Override
					public void completed(Integer result, ByteBuffer buffer) {
							Asynchronous.this.bytesRead = result;
							if (Asynchronous.this.bytesRead < 0)
									return;

							buffer.flip();

							byte[] data = new byte[buffer.limit()];
							buffer.get(data);

							Asynchronous.this.consumer.accept(BytesPayload.from(data, data.length));
							buffer.clear();
							Asynchronous.this.position = Asynchronous.this.position + Asynchronous.this.bytesRead;
							Asynchronous.this.fileChannel.read(buffer, Asynchronous.this.position, buffer, this);
					}
			};

		Asynchronous(Consumer<BytesPayload> consumer) {
				this.consumer = consumer;
		}

		public void read(File file) throws IOException {
				Path path = file.toPath();
				this.fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
				ByteBuffer buffer = ByteBuffer.allocate(FileCopyUtils.BUFFER_SIZE);
				this.fileChannel.read(buffer, position, buffer, completionHandler);
				while (this.bytesRead > 0) {
						this.position = this.position + this.bytesRead;
						this.fileChannel.read(buffer, this.position, buffer, this.completionHandler);
				}
		}
}