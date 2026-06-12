package dev.l3g7.griefer_utils.core.api.util.io;

import dev.l3g7.griefer_utils.core.api.bridges.LabyBridge;
import dev.l3g7.griefer_utils.core.api.misc.CustomSSLSocketFactoryProvider;
import dev.l3g7.griefer_utils.core.api.misc.Result;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * A wrapper class for reading the contents of a URL using HTTP GET.
 */
public class HttpGetOperation extends ReadOperation {

	private final String url;
	private HttpURLConnection connection = null;

	HttpGetOperation(String url) {
		this.url = url;
	}

	private HttpURLConnection open() throws URISyntaxException, IOException {
		if (connection != null)
			return connection;

		connection = (HttpURLConnection) new URI(url).toURL().openConnection();

		if (connection instanceof HttpsURLConnection)
			((HttpsURLConnection) connection).setSSLSocketFactory(CustomSSLSocketFactoryProvider.getCustomFactory());

		connection.addRequestProperty("User-Agent", IO.getUserAgent());
		connection.setConnectTimeout(10000);
		return connection;
	}

	@Override
	protected String getType() {
		return "HTTP GET";
	}

	/**
	 * @return the HTTP response code, or -1 if the request failed.
	 */
	public int getResponseCode() {
		return Result.tryGet(this::open)
			.map(HttpURLConnection::getResponseCode)
			.unwrapOr(-1);
	}

	@Override
	protected InputStream getIn() throws Exception {
		return open().getInputStream();
	}

	@Override
	protected long predictSize() {
		return Result.tryGet(() -> Long.parseLong(open().getHeaderField("Content-Length")))
			.unwrapOr(FALLBACK_SIZE);
	}

}
