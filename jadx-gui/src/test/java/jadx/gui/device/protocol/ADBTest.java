package jadx.gui.device.protocol;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ADBTest {
	@Test
	void deviceListenerThreadTerminatesAfterDisconnect() throws Exception {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			Thread serverThread = new Thread(() -> serveTrackDevices(serverSocket));
			serverThread.setDaemon(true);
			serverThread.start();

			CountDownLatch disconnected = new CountDownLatch(1);
			AtomicReference<Thread> callbackThread = new AtomicReference<>();
			ADB.DeviceStateListener listener = new ADB.DeviceStateListener() {
				@Override
				public void onDeviceStatusChange(List<ADBDeviceInfo> deviceInfoList) {
				}

				@Override
				public void adbDisconnected() {
					callbackThread.set(Thread.currentThread());
					disconnected.countDown();
				}
			};

			try (Socket ignored = ADB.listenForDeviceState(
					listener, "127.0.0.1", serverSocket.getLocalPort())) {
				assertThat(disconnected.await(2, TimeUnit.SECONDS)).isTrue();
				Thread thread = callbackThread.get();
				assertThat(thread).isNotNull();
				assertThat(thread.isDaemon()).isTrue();
				thread.join(1_000);
				assertThat(thread.isAlive()).isFalse();
			}
		}
	}

	private static void serveTrackDevices(ServerSocket serverSocket) {
		try (Socket socket = serverSocket.accept()) {
			InputStream input = socket.getInputStream();
			byte[] command = "0014host:track-devices-l".getBytes(StandardCharsets.UTF_8);
			assertThat(input.readNBytes(command.length)).isEqualTo(command);
			socket.getOutputStream().write("OKAY".getBytes(StandardCharsets.UTF_8));
			socket.getOutputStream().flush();
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
}
