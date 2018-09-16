package ywcai.ls.gateway;

import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;

//@Component
public class MyFilter extends ZuulFilter{
	public MyFilter()
	{
		System.out.println("init=============myfilter=============");
	}
	@Override
	public boolean shouldFilter() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Object run() {
		// TODO Auto-generated method stub
		System.out.println("test .......................");
		return null;
	}

	@Override
	public String filterType() {
		// TODO Auto-generated method stub
		return "pre";
	}

	@Override
	public int filterOrder() {
		// TODO Auto-generated method stub
		return -5;
	}

}
