package ywcai.ls.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import com.netflix.zuul.filters.FilterRegistry;

@SpringBootApplication
@EnableZuulProxy
public class GatwayApplication {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(GatwayApplication.class, args);
		removeDefaultRibbonFilter();
	}
	private static void removeDefaultRibbonFilter() {
		FilterRegistry r=FilterRegistry.instance();
		r.remove("ribbonRoutingFilter");
	}
}
