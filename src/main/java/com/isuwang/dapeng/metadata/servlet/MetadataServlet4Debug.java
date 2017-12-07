package com.isuwang.dapeng.metadata.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.isuwang.dapeng.code.parser.ThriftCodeParser;
import com.isuwang.dapeng.core.SoaException;
import com.isuwang.dapeng.core.metadata.Service;
import com.isuwang.dapeng.metadata.beans.FieldMeta;
import com.isuwang.dapeng.metadata.beans.MetaDto;
import com.isuwang.dapeng.metadata.util.GsonBooleanAdapter;
import com.isuwang.dapeng.metadata.util.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wahsonleung hsliang@163.com
 */
public class MetadataServlet4Debug extends MetadataServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataServlet4Debug.class);

    private String thriftIn;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.thriftIn = config.getInitParameter("thriftIn");
    }

    @Override
    protected Service getService(String serviceName, String version) throws Exception {
        String[] resources = (String[]) Arrays.asList(new File(this.thriftIn).listFiles((dir, name) -> name.endsWith(".thrift")))
                .stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList()).toArray();

        List<Service> found = new ThriftCodeParser("java").toServices(resources, "1.0.0")
                .stream()
                .filter(service -> (service.namespace + "." + service.name).equals(serviceName))
                .collect(Collectors.toList());
        if(found.size() == 0) {
            throw new Exception("Service not found: " + serviceName);
        }
        return found.get(0);
    }
}
