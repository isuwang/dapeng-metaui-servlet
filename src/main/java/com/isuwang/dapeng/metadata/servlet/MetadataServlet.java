package com.isuwang.dapeng.metadata.servlet;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.isuwang.dapeng.core.SoaException;
import com.isuwang.dapeng.core.metadata.Service;
import com.isuwang.dapeng.metadata.beans.FieldMeta;
import com.isuwang.dapeng.metadata.beans.MetaDto;
import com.isuwang.dapeng.metadata.util.GsonBooleanAdapter;
import com.isuwang.dapeng.metadata.util.MetadataUtils;
import com.isuwang.dapeng.remoting.fake.metadata.MetadataClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wahsonleung hsliang@163.com
 */
public class MetadataServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataServlet.class);

    private ServletConfig config;
    private String username;
    private String driverClassName;
    private String url;
    private String password;
    private Connection conn;
    private Statement stmt;
    private ResultSet rs;

    private Gson gson;
    private Properties props;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        //从配置文件中获取JDBC驱动名
        this.driverClassName = this.config.getInitParameter("driverClassName");

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Boolean.class, new GsonBooleanAdapter());
        gsonBuilder.registerTypeAdapter(boolean.class, new GsonBooleanAdapter());
        this.gson = gsonBuilder.create();

        try {

            // 获取要连接的数据库
            this.url = this.getProperties("DB_METADB_URL");
            // 获取数据库用户名
            this.username = this.getProperties("DB_METADB_USER");
            // 获取数据库连接密码
            this.password = this.getProperties("DB_METADB_PASSWD");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getProperties(String key) throws IOException {
        String value = System.getProperty(key);
        if(value == null) {
            if(this.props == null) {
                this.props = new Properties();
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("init.properties");
                if(in == null) {
                    LOGGER.error("Count not resolve file init.properties");
                    throw new FileNotFoundException("init.properties not found");
                }
                this.props.load(in);
            }

            value = this.props.getProperty(key);
        }
        return value;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        String serviceName = req.getParameter("serviceName");
        String version = req.getParameter("version");

        if (serviceName == null) {
            resp.setStatus(400);
            out.write("Parameter[serviceName] is null");
            return;
        }

        if (version == null) {
            resp.setStatus(400);
            out.write("Parameter[version] is null");
            return;
        }

        try {

            Service service = getService(serviceName, version);
            String structsSqlIn = service.getStructDefinitions()
                    .stream().map(struct -> "'" + struct.namespace + "." + struct.name + "'")
                    .collect(Collectors.joining(",", "(" ,")"));

            Class.forName(this.driverClassName);
            this.conn = DriverManager.getConnection(this.url, this.username, this.password);
            String sql = "SELECT * FROM metadb.fields WHERE `struct_name` in " + structsSqlIn;
            this.stmt = this.conn.createStatement();
            this.rs = this.stmt.executeQuery(sql);

            ResultSetMetaData metaData = this.rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<FieldMeta> data = new ArrayList<>();
            while (this.rs.next()) {
                Map<String, Object> map = new HashMap<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    map.put(MetadataUtils.parseFieldName(metaData.getColumnName(i)), this.rs.getObject(i));
                }
                FieldMeta fm = this.gson.fromJson(this.gson.toJson(map), FieldMeta.class);
                data.add(fm);
            }

            Map<String, List<FieldMeta>> fieldsInStruct =
                    data.stream().collect(Collectors.groupingBy(fm -> fm.getStructName(), Collectors.toList()));

            MetaDto md = new MetaDto();
            md.setServiceMeta(service);
            md.setFieldsInStruct(fieldsInStruct);

            out.write(this.gson.toJson(md));
            out.flush();
            out.close();
            this.rs.close();
            this.stmt.close();
            this.conn.close();
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage());
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        } catch (SoaException e) {
            LOGGER.error(e.getMessage());
            throw new ServletException(e.getMessage());
        }
    }

    @Override
    public void destroy() {
        this.config = null;
        this.rs = null;
        this.conn = null;
        this.stmt = null;
    }

    private Service getService(String serviceName, String version) throws SoaException {
        String metaXml = new MetadataClient(serviceName, version).getServiceMetadata();
        StringReader reader = new StringReader(metaXml);
        return JAXB.unmarshal(reader, Service.class);
    }
}
