package com.isuwang.dapeng.metadata.servlet;

import com.isuwang.dapeng.code.parser.ThriftCodeParser;
import com.isuwang.dapeng.core.metadata.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.bind.JAXB;
import java.io.*;
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
        LOGGER.info("MetadataServlet4Debug thriftIn = {}", this.thriftIn);
    }

    @Override
    protected Service getService(String serviceName, String version) throws Exception {

        File[] files = new File(this.thriftIn).listFiles((dir, name) -> name.endsWith(".thrift"));
        String[] resources = new String[files.length];

        Arrays.asList(files)
                .stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList()).toArray(resources);

        List<Service> found = new ThriftCodeParser("java").toServices(resources, "1.0.0")
                .stream()
                .filter(service -> (service.namespace + "." + service.name).equals(serviceName))
                .collect(Collectors.toList());
        if (found.size() == 0) {
            throw new Exception("Service not found: " + serviceName);
        }

        // so stupid,  this.gson.toJson(found.get(0).getStructDefinitions()) return [null,null ...] ???
        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        JAXB.marshal(found.get(0), xml);
        InputStream in = new ByteArrayInputStream(xml.toByteArray());
        return JAXB.unmarshal(in, Service.class);
    }
}
