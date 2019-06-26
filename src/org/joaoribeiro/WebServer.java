package org.joaoribeiro;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {

    private ServerSocket serverSocket;

    private Socket clientSocket;

    private BufferedReader in;

    private PrintWriter out;

    public WebServer() {

        try {

            serverSocket = new ServerSocket(8085);

        } catch (IOException ex) {
            System.out.println("IO Exception while setting a new server socket.");
        }

    }

    public void start() {

        String request = "";

        String response = "";

        String[] reqArray;

        try {

            listen();

            request = in.readLine();

            System.out.println(request);

        } catch (IOException ex) {
            System.out.println("IO exception");
        }

        reqArray = request.split(" ");

        System.out.println(reqArray[0]);

        String reqType = reqArray[0];

        switch (reqType) {
            case "GET":
                get(reqArray);
                break;
            default:
                break;
        }

        close();
        start();

    }

    private void listen() {

        try {

            clientSocket = serverSocket.accept();

            setupIOStreams();

        } catch (IOException ex) {
            System.out.println("IO exception");
        }

    }

    private void close() {

        try {
            clientSocket.close();
        } catch (IOException ex) {
            System.out.println("IO exception trying to close connection.");
        }

    }

    private void get(String[] reqArray) {

        ReqType reqType;

        if (reqArray[1].equals("/")) {

            reqArray[1] = "www/index.html";

            reqType = ReqType.HTML;

        } else {

            reqType = requestType(reqArray[1]);

            reqArray[1] = "www" + reqArray[1];
        }

        switch (reqType) {
            case HTML:
                getHTML(reqArray[1]);
                break;
            case IMAGE:
                getImage(reqArray[1]);
                break;
        }

    }

    private void getHTML(String path) {

        Status code = getStatusCode(path);

        switch (code) {
            case OK:

                out.println(buildHeader(Status.OK, getFileSize(path), ReqType.HTML, path));

                try {
                    serveHTML(path);
                } catch (IOException ex) {
                    System.out.println("IO Exception trying to read HTML file.");
                }

                break;
            case CLIENT_ERROR:

                String pathError = "www/4xx.html";

                System.out.println(buildHeader(Status.CLIENT_ERROR, getFileSize(pathError), ReqType.HTML, path));

                out.println(buildHeader(Status.CLIENT_ERROR, getFileSize(pathError), ReqType.HTML, path));

                try {
                    serveHTML(pathError);
                } catch (IOException ex) {
                    System.out.println("IO Exception trying to read 404 file.");
                }

                break;
            default:
                break;
        }

    }

    private void getImage(String path) {

        System.out.println(buildHeader(Status.OK, getFileSize(path), ReqType.IMAGE, path));

        out.println(buildHeader(Status.OK, getFileSize(path), ReqType.IMAGE, path));


        System.out.println(getFileSize(path));

        try {
            serveImage(path);
        } catch (IOException ex) {
            System.out.println("IO Exception trying to send Image file.");
        }

    }

    private ReqType requestType(String request) {

        if(request.length() < 5) {
            System.out.println("Can't find resource.");
            return ReqType.HTML;
        }

        String extension = request.substring(request.length() - 3);

        if (extension.equals("jpg") ||
            extension.equals("bmp") ||
            extension.equals("gif") ||
            extension.equals("png")) {

            return ReqType.IMAGE;

        }

        return ReqType.HTML;

    }

    private String getImgExtension(String path) {

        return path.substring(path.length() - 3);
    }

    private String buildHeader(Status code, long contentLength, ReqType type, String path) {

        switch (type) {
            case HTML:
                return "HTTP/1.0 " + code.getCode() + " Document Follows\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: " + contentLength + "\r\n" +
                        "\r\n";
            case IMAGE:
                return "HTTP/1.0 " + code.getCode() + " Document Follows\r\n" +
                        "Content-Type: image/" + getImgExtension(path) + "\r\n" +
                        "Content-Length: " + contentLength + "\r\n" +
                        "\r\n";
            default:
                break;
        }

        return "";
    }

    private void setupIOStreams() throws IOException {

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    private void serveHTML(String path) throws IOException {

        String line;

        BufferedReader htmlReader = new BufferedReader(new FileReader(path));

        while ((line = htmlReader.readLine()) != null) {

            out.println(line);

        }

    }

    private void serveImage(String path) throws IOException{

        System.out.println(path);

        FileInputStream fileInputStream = new FileInputStream(path);

        BufferedInputStream bStream = new BufferedInputStream(fileInputStream);

        DataOutputStream dOutStream = new DataOutputStream(clientSocket.getOutputStream());

        int num;

        byte[] buffer = new byte[1024];

        while ((num = bStream.read(buffer)) > 0) {

            System.out.println(num);
            dOutStream.write(buffer, 0, num);
        }

        dOutStream.flush();
        dOutStream.close();
    }

    private long getFileSize(String path) {

        File file = new File(path);

        return file.length();

    }

    private boolean checkResource(String path) {

        File resource = new File(path);

        return resource.exists();

    }

    private Status getStatusCode(String path) {

        if (checkResource(path)) {
            return Status.OK;
        }

        return Status.CLIENT_ERROR;

    }

}
