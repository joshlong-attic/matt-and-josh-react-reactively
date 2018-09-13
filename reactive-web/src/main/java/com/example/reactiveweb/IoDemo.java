package com.example.reactiveweb;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

/**
	* @author <a href="mailto:josh@joshlong.com">Josh Long</a>
	*/
@Log4j2
public class IoDemo {


		private static void doSomethingWithBytes(byte bytes[]) {
				log.info(String.format("new bytes available! got %d bytes.", bytes.length));
		}

		private static void synchronousIo(File file) throws Exception {
				try (FileInputStream in = new FileInputStream(file)) {
						int nRead;
						byte[] data = new byte[FileCopyUtils.BUFFER_SIZE];
						while ((nRead = in.read(data, 0, data.length)) != -1) {
								doSomethingWithBytes(data);
						}
				}
		}

		private static void asynchronousIoWithFuture(File file) throws Exception {
				Path path = file.toPath();
				ByteBuffer allocate = ByteBuffer.allocate(FileCopyUtils.BUFFER_SIZE);
				AsynchronousFileChannel open = AsynchronousFileChannel.open(path);
				int pos = 0;
				do {
						Future<Integer> read = open.read(allocate, pos);
						Integer integer = read.get();
						if (integer < 0) break;
						pos += integer;
						allocate.flip();
						doSomethingWithBytes(allocate.array());
						allocate.clear();
				}
				while (true);
		}


		private static void asynchronousFileIo1(File file) throws Exception {
				ExecutorService pool = new ScheduledThreadPoolExecutor(3);
				AsynchronousFileChannel fileChannel = AsynchronousFileChannel
					.open(file.toPath(), EnumSet.of(StandardOpenOption.READ), pool);
				CompletionHandler<Integer, ByteBuffer> handler = new CompletionHandler<Integer, ByteBuffer>() {
						@Override
						public void completed(Integer result, ByteBuffer attachment) {
								for (int i = 0; i < attachment.limit(); i++) {
										System.out.println((char) attachment.get(i));
								}
						}

						@Override
						public void failed(Throwable e, ByteBuffer attachment) {
						}
				};
				final int bufferCount = 5;
				ByteBuffer buffers[] = new ByteBuffer[bufferCount];
				for (int i = 0; i < bufferCount; i++) {
						buffers[i] = ByteBuffer.allocate(10);
						fileChannel.read(buffers[i], i * 10, buffers[i], handler);
				}

		}

		@Deprecated
		private static void asynchronousIo(File file) throws Exception {
				CompletionHandler<Integer, ByteBuffer> handler = new CompletionHandler<Integer, ByteBuffer>() {

						@Override
						public void completed(Integer result, ByteBuffer attachment) {
								attachment.flip();
								if (attachment.hasRemaining()) {
										byte[] data = new byte[attachment.remaining()];
										attachment.get(data);
										doSomethingWithBytes(data);
								}
								attachment.clear();
						}

						@Override
						public void failed(Throwable exc, ByteBuffer attachment) {
								log.error(exc);
						}
				};

				try (AsynchronousFileChannel fileChannel =
										AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						long position = 0;
						fileChannel.read(buffer, position, buffer, handler);
				}


		}

		public static void main(String args[]) throws IOException {
				try {
						String home = System.getenv("HOME");
						File desktop = new File(home, "Desktop");
						File inputFile = new File(desktop, "input.txt");
						log.info("-------------------------------");
						synchronousIo(inputFile);
						log.info("-------------------------------");
						AsyncFileIo.read(inputFile, IoDemo::doSomethingWithBytes);
				}
				catch (Exception e) {
						ReflectionUtils.rethrowRuntimeException(e);
				}
				System.in.read();
		}
}

@Log4j2
class AsyncFileIo {

		static class MyCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

				private final Consumer<byte[]> consumer;

				MyCompletionHandler(Consumer<byte[]> consumer) {
						this.consumer = consumer;
				}

				@Override
				public void failed(Throwable exc, ByteBuffer attachment) {
				}

				@Override
				public void completed(Integer result, ByteBuffer buffer) {
						bytesRead = result;
						if (bytesRead < 0)
								return;

						buffer.flip();

						byte[] data = new byte[buffer.limit()];
						buffer.get(data);

						// do something with data
						consumer.accept(data);
						buffer.clear();
						position = position + bytesRead;
						fileChannel.read(buffer, position, buffer, this);
				}

		}

		private static int bytesRead = 0;
		private static long position = 0;
		private static AsynchronousFileChannel fileChannel = null;

		public static void read(File file, Consumer<byte[]> consumer) {
				Path path = file.toPath();

				try {
						fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
						ByteBuffer buffer = ByteBuffer.allocate(1024);

						MyCompletionHandler myCompletionHandler = new MyCompletionHandler(consumer);
						fileChannel.read(buffer, position, buffer, myCompletionHandler);
						// read() returns -1 if End of File is reached.
						while (bytesRead > 0) {
								// Update to new read position.
								position = position + bytesRead;
								fileChannel.read(buffer, position, buffer, myCompletionHandler);
						}
				}
				catch (IOException e) {
						e.printStackTrace();
				}

		}

}
