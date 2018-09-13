package com.example.io;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

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
