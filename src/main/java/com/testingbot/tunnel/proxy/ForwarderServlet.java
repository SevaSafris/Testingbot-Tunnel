package com.testingbot.tunnel.proxy;

import com.testingbot.tunnel.App;
import org.eclipse.jetty.proxy.AsyncProxyServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.HttpExchange;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpURI;

public class ForwarderServlet extends AsyncProxyServlet {
    private App app;
    
    public ForwarderServlet(App app) {
        this.app = app;
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    @Override
    protected String rewriteTarget(HttpServletRequest request)
    {   
        return "http://127.0.0.1:4446" + request.getRequestURI();
    }
    
    @Override
    protected void addProxyHeaders(HttpServletRequest clientRequest, Request proxyRequest)
    {
        addViaHeader(proxyRequest);
        addXForwardedHeaders(clientRequest, proxyRequest);
        proxyRequest.header("TB-Tunnel", this.app.getServerIP());
        proxyRequest.header("TB-Credentials", this.app.getClientKey() + "_" + this.app.getClientSecret());
        if (this.app.isBypassingSquid()) {
            proxyRequest.header("TB-Tunnel-Port", "2010");
        }
       
        Logger.getLogger(ForwarderServlet.class.getName()).log(Level.INFO, " >> [{0}] {1}", new Object[]{clientRequest.getMethod(), clientRequest.getRequestURL()});
    }

    @Override
    protected void onClientRequestFailure(HttpServletRequest clientRequest, Request proxyRequest, HttpServletResponse proxyResponse, Throwable failure)
    {
        super.onClientRequestFailure(clientRequest, proxyRequest, proxyResponse, failure);
        Logger.getLogger(ForwarderServlet.class.getName()).log(Level.WARNING, "Error when forwarding request: {0} {1}", new Object[]{failure.getMessage(), failure.getStackTrace().toString()});
    }
}
