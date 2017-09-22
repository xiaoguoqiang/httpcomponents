package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * 
 * Http 客户端 下载文件工具类
 * @author liqianga
 * @version v1.0
 * **/

public class HttpClient {

	public static final int cache = 10 * 1024;
	public static final boolean isWindow;
	public static final String splash;
	public static final String root;
	static {
		if (System.getProperty("os.name") != null && System.getProperty("os.name").toLowerCase().contains("windows")) {
			isWindow = true;
			splash = "\\";
			root = "D:";
		} else {
			isWindow = false;
			splash = "/";
			root = "/search";
		}

	}

	/**
	 * 根据url获取指定文件，并存入filePath中
	 * 
	 **/
	public static void download(String url, String filePath) {

		CloseableHttpClient client = HttpClients.createDefault();
		try {
			//get 访问url
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = client.execute(httpGet); 
			
			//获取文件输入流
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			
			if(filePath == null){
				filePath = getFilePath(response);
			}
			//初始化输出路径和输出流
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(file);
			//写入文件
			byte[] buffer = new byte[cache];
			int n = 0;
			while((n=in.read(buffer)) != -1){
				out.write(buffer, 0, n);
			}
			
			//资源释放
			in.close();
			out.flush();
			out.close();

		}catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 获取response要下载的文件的默认路径
	 * 
	 * @param response
	 * @return
	 */
	public static String getFilePath(HttpResponse response) {
		String filepath = root + splash;
		String filename = getFileName(response);

		if (filename != null) {
			filepath += filename;
		} else {
			filepath += getRandomFileName();
		}
		return filepath;
	}

	/**
	 * 获取response header中Content-Disposition中的filename值
	 * 
	 * @param response
	 * @return
	 */
	public static String getFileName(HttpResponse response) {
		Header contentHeader = response.getFirstHeader("Content-Disposition");
		String filename = null;
		if (contentHeader != null) {
			HeaderElement[] values = contentHeader.getElements();
			if (values.length == 1) {
				NameValuePair param = values[0].getParameterByName("filename");
				if (param != null) {
					try {
						// filename = new
						// String(param.getValue().toString().getBytes(),
						// "utf-8");
						// filename=URLDecoder.decode(param.getValue(),"utf-8");
						filename = param.getValue();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return filename;
	}

	/**
	 * 获取随机文件名
	 * 
	 * @return
	 */
	public static String getRandomFileName() {
		return String.valueOf(System.currentTimeMillis());
	}

	public static void outHeaders(HttpResponse response) {
		Header[] headers = response.getAllHeaders();
		for (int i = 0; i < headers.length; i++) {
			System.out.println(headers[i]);
		}
	}

	// http://10.10.1.68:50000/download/t1230v2contact?version=T1230V2Contact_11
	public static void main(String[] args) {
		String url = "http://192.168.90.62:8080/test/test.xls";
		String filePath = "D:\\testserver\\test.xls";
		HttpClient.download(url, filePath);
	}

}
