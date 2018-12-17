package gl.ao.addi;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import gl.ao.addi.query.QueryEngine;
import org.json.JSONArray;
import org.json.JSONObject;

public class Server {
    public void serve() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(1985), 0);
            server.createContext("/", new RootHandler());
            server.createContext("/interactive", new InteractiveHandler());
            server.createContext("/get", new GenericGetHandler());
            server.createContext("/post", new GenericPostHandler());
            server.createContext("/put", new GenericPutHandler());
            server.createContext("/delete", new GenericDeleteHandler());
            server.setExecutor(null);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class RootHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

            JSONObject jsonObjThis = new JSONObject();

            jsonObjThis.put("version", "0.0.1");
            jsonObjThis.put("started", Construct.currentDate);

            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            String formattedDate=dateFormat. format( new Date().getTime() - Construct.currentDate.getTime() );
            jsonObjThis.put("uptime", formattedDate);

            InetAddress IP=InetAddress.getLocalHost();
            jsonObjThis.put("ip", IP.getHostAddress());
            jsonObjThis.put("hostname", IP.getHostName());

            //disk
            JSONObject jsonObjectThisDisk = new JSONObject();
            try {
                File f = new File("/");
                jsonObjectThisDisk.put("free", f.getFreeSpace()/1024/1024/1024+"G ("+f.getFreeSpace()+"bytes)");
                jsonObjectThisDisk.put("usable", f.getUsableSpace()/1024/1024/1024+"G ("+f.getUsableSpace()+"bytes)");
                jsonObjectThisDisk.put("total", f.getTotalSpace()/1024/1024/1024+"G ("+f.getTotalSpace()+"bytes)");
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonObjThis.put("disk", jsonObjectThisDisk);

            //processors
            jsonObjThis.put("processors", Runtime.getRuntime().availableProcessors());

            //memory
            JSONObject jsonObjectThisMemory = new JSONObject();
            try {
                long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
                long freeMemorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
                jsonObjectThisMemory.put("total", memorySize/1024/1024/1024+"G ("+memorySize+"bytes)");
                jsonObjectThisMemory.put("free", freeMemorySize/1024/1024/1024+"G ("+freeMemorySize+"bytes)");
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonObjThis.put("memory", jsonObjectThisMemory);

            //os
            JSONObject jsonObjectThisOS = new JSONObject();
            try {
                String OSName = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getName();
                String OSVersion = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getVersion();
                String OSArch = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getArch();
                jsonObjectThisOS.put("name", OSName);
                jsonObjectThisOS.put("version", OSVersion);
                jsonObjectThisOS.put("arch", OSArch);
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonObjThis.put("os", jsonObjectThisOS);

            //heath
            jsonObjThis.put("health", "green");


            JSONObject jsonObjRoot = new JSONObject();
            jsonObjRoot.put("_", "Autonomous Distributed Database");
            jsonObjRoot.put("this", jsonObjThis);
            jsonObjRoot.put("totalNodes", Integer.toString(Construct.network.nodes.size()));
            jsonObjRoot.put("nodes", Construct.network.nodes);

            //response
            String response = jsonObjRoot.toString();
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class InteractiveHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = Interactive.IndexTemplate("http://"+t.getRequestHeaders().getFirst("Host"), t.getRequestURI().getPath());
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static void GenericMethodHandlerFailure(String expected, int statusCode, HttpExchange t) {
        try {
            String response = "Got "+t.getRequestMethod()+", expected "+expected;
            t.sendResponseHeaders(statusCode, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class GenericGetHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String expected = "GET";
            if (t.getRequestMethod().equals(expected)) {
                String response = "GET";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                Server.GenericMethodHandlerFailure(expected, 400, t);
            }
        }
    }

    static class GenericPostHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String expected = "POST";
            if (t.getRequestMethod().equals(expected)) {

                String response = "POST";

                if (t.getRequestURI().toString().startsWith("/post?query")) {
                    //interactive!
                    InputStream inputStream = t.getRequestBody();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[2048];
                    int read = 0;
                    while ((read = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, read);
                    }

                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(byteArrayOutputStream.toString());
                        List<JSONObject> list = QueryEngine.query((String) jsonObj.get("value"));
                        JSONArray jsonArray = new JSONArray(list);
                        response = jsonArray.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                Server.GenericMethodHandlerFailure(expected, 400, t);
            }
        }
    }
    static class GenericPutHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String expected = "PUT";
            if (t.getRequestMethod().equals(expected)) {
                String response = "PUT";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                Server.GenericMethodHandlerFailure(expected, 400, t);
            }
        }
    }
    static class GenericDeleteHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String expected = "DELETE";
            if (t.getRequestMethod().equals(expected)) {
                String response = "DELETE";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                Server.GenericMethodHandlerFailure(expected, 400, t);
            }
        }
    }

    static Map<String, String> getParameters(HttpExchange httpExchange) {
        Map<String, String> parameters = new HashMap<>();
        try {
            InputStream inputStream = httpExchange.getRequestBody();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[2048];
            int read = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, read);
            }
            String[] keyValuePairs = byteArrayOutputStream.toString().split("&");
            for (String keyValuePair : keyValuePairs) {
                String[] keyValue = keyValuePair.split("=");
                if (keyValue.length != 2) {
                    continue;
                }
                parameters.put(keyValue[0], keyValue[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

}
