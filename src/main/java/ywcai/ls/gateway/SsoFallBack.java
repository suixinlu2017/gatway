package ywcai.ls.gateway;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.cloud.netflix.zuul.filters.route.ZuulFallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class SsoFallBack implements ZuulFallbackProvider  {

	@Override
	public String getRoute() {
		// TODO Auto-generated method stub
		return "sso";
	}

	@Override
	public ClientHttpResponse fallbackResponse() {
		// TODO Auto-generated method stub
		return new ClientHttpResponse() {
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return HttpStatus.OK;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return this.getStatusCode().value();
			}

			@Override
			public String getStatusText() throws IOException {
				return this.getStatusCode().getReasonPhrase();
			}

			@Override
			public void close() {

			}

			@Override
			public InputStream getBody() throws IOException {
				System.out.println("========sso服务不可用===========");
				return new ByteArrayInputStream("sso服务不可用，请稍后再试！<br/><a href='http://localhost/sso/index'>刷新首页</alink>".getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				MediaType mt = new MediaType("text", "html", Charset.forName("UTF-8"));
				headers.setContentType(mt);
				return headers;
			}
		};
	}

}
