package com.isuwang.dapeng.metadata.servlet;

import com.google.gson.Gson;
import com.isuwang.dapeng.metadata.beans.ClientInvokeRequest;
import com.github.dapeng.service.invoke.BaseController;
import com.github.dapeng.service.invoke.entity.BaseRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * @author wahsonleung
 * @date 2017-12-20
 */
public class SoaClientInvokeServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InputStreamReader br = new InputStreamReader(req.getInputStream());
        ClientInvokeRequest request = gson.fromJson(br, ClientInvokeRequest.class);

        String parameters = null;
        if (request.getParameters() != null) {
            parameters = gson.toJson(request.getParameters());
        }

        System.setProperty("soa.zookeeper.host","192.168.4.132:2181");

        BaseRequest baseRequest = new BaseRequest(parameters, request.getService(), request.getVersion(), request.getMethod());

        String result = BaseController.invoke(baseRequest);
        System.out.println(result);

        resp.setContentType("application/json;charset=utf-8");
        PrintWriter pw = resp.getWriter();
        pw.write(result);
    }
}
