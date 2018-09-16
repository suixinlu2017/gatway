package ywcai.ls.gateway;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandContext;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonCommandFactory;
import org.springframework.cloud.netflix.zuul.filters.route.RibbonRoutingFilter;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class FixRibbonRoutingFilter extends RibbonRoutingFilter{
	@Autowired
	ProxyRequestHelper helper;
	@Autowired
	RibbonCommandFactory<?> ribbonCommandFactory;
	String defaultSuccessUrl="/index";
	public void setDefaultSuccessUrl(String url)
	{
		if(url.equals("/")||url.equals(""))
		{
			return ;
		}
		this.defaultSuccessUrl=url.startsWith("/")?url:"/"+url;
	}
	public FixRibbonRoutingFilter(ProxyRequestHelper helper, RibbonCommandFactory<?> ribbonCommandFactory) {
		super(helper, ribbonCommandFactory,Collections.emptyList());
		// TODO Auto-generated constructor stub
	}
	private void addPathCache(String requestPath,String requestServiceId )
	{
		if(requestPath.equals("/")||requestPath.equals(""))
		{
			requestPath=defaultSuccessUrl;
		}
		HttpSession cache=RequestContext.getCurrentContext().getRequest().getSession();
		if(!isHasCache(requestPath))
		{
			cache.setAttribute(requestPath,requestServiceId);
		}
	}
	private boolean isHasCache(String requestPath)
	{
		HttpSession cache=RequestContext.getCurrentContext().getRequest().getSession();
		return cache.getAttribute(requestPath)!=null?true:false;
	}

	private String getServiceIdAndRemove(String requestPath)
	{
		HttpSession cache=RequestContext.getCurrentContext().getRequest().getSession();
		String serviceId="";
		if(isHasCache(requestPath))
		{
			serviceId= (String) cache.getAttribute(requestPath);
			cache.removeAttribute(requestPath);
		}
		return serviceId;
	}

	private void assembleRealPath(ClientHttpResponse response, URI location,
			String nowPath,String serviceId) {
		// TODO Auto-generated method stub
		int nowPort=location.getPort()<=0?80:location.getPort();
		String newPath=
				location.getScheme()+"://"+location.getHost()+":"
						+nowPort+"/"+serviceId+nowPath;
		newPath=location.getQuery()==null?newPath:(newPath+"?"+location.getQuery());
		newPath=location.getFragment()==null?newPath:(newPath+"#"+location.getFragment());
		URI newLocation=null;
		try {
			newLocation = new URI(newPath);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.getHeaders().setLocation(newLocation);
	}

	@Override
	public Object run() {
		// TODO Auto-generated method stub
		RequestContext context = RequestContext.getCurrentContext();
		this.helper.addIgnoredHeaders();
		try {
			RibbonCommandContext commandContext = buildCommandContext(context);
			String preUrl=commandContext.getUri();
			//如果是登录页面，则缓存记录当前访问的服务ID，只会记录这个SESSION访问的第一个ID
			if(preUrl.equals("/login"))
			{
				addPathCache(defaultSuccessUrl,commandContext.getServiceId());
			}
			ClientHttpResponse response = forward(commandContext);
			URI location=response.getHeaders().getLocation();	
			if(response.getStatusCode()==HttpStatus.FOUND&&location!=null)
			{	
				//如果是被重定向了，则记录之前的路径
				String nowPath=location.getPath();
				if(nowPath.equals("/login"))
				{
					String serviceId=commandContext.getServiceId();
					addPathCache(preUrl,serviceId);
					assembleRealPath(response,location,nowPath,  serviceId);	 
				}
				//如果是缓存过这个页面，则获取缓存路径重新封装并重定向到缓存位置
				else if(isHasCache(nowPath))
				{
					String serviceId=getServiceIdAndRemove(nowPath);
					assembleRealPath(response,location,nowPath,serviceId);
				}
				//如果资源是"/"或为空，则代表是直接在浏览器输入login页面登录的，转到默认页面。
				else if(nowPath.equals("/")||nowPath.equals(""))
				{
					String serviceId=getServiceIdAndRemove(defaultSuccessUrl);
					assembleRealPath(response,location,defaultSuccessUrl,serviceId);
				}
				//其他情况则什么也不做
				else
				{
 
				}
			}
			setResponse(response);
			return response;
		}
		catch (ZuulException ex) {
			throw new ZuulRuntimeException(ex);
		}
		catch (Exception ex) {
			throw new ZuulRuntimeException(ex);
		}
	}


}
