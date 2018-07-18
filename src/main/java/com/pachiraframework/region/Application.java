package com.pachiraframework.region;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

/**
 * Hello world!
 *
 */
public class Application {
	private File result = new File("d:/r.txt");
	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
		Application application = new Application();
		application.parseProvinces();
		
		
	}

	/**
	 * 解析省份数据
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws InterruptedException 
	 */
	private void parseProvinces() throws MalformedURLException, IOException, InterruptedException {
		Document document = parse("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/index.html");
		Elements elements = document.getElementsByClass("provincetr");
		for(int i = 0;i < elements.size();i++) {
			Element tr = elements.get(i);
			Elements tds = tr.getElementsByTag("td");
			for(int j = 0;j < tds.size();j++) {
				//<td><a href="11.html">北京市<br></a></td>
				Element td = tds.get(j);
				Elements as = td.getElementsByTag("a");
				for(int k = 0; k < as.size();k++) {
					Element a = as.get(k);
//					System.out.println(a);
					String href = a.attr("href");
					String code = href.substring(0, href.indexOf("."));
					String text = a.text();
//					text = text.substring(0, text.indexOf("<br>"));
					Region region = Region.builder().code(code+"0000000000").parent(code+"0000000000").level(1).name(text).build();
					output(region);
					parseCities(href);
					markDone(region.getCode());
					System.out.println("#################################");
					System.out.println("\t\t"+region.getCode()+"\t\t处理完成");
				}
			}
		}
	}
	
	private Document parse(String url) {
		try {
			Document document = Jsoup.parse(new URL(url), 100000);
			return document;
		}catch(Exception e) {
			System.out.println(e);
			return parse(url);
		}
	}
	
	private void markDone(String code) throws IOException {
		Files.asCharSink(result, Charset.defaultCharset(), FileWriteMode.APPEND).write(code+"\n"); 
	}
	
	private void output(Region region) throws FileNotFoundException, IOException {
//		String fileName = "d:/regions/"+region.getCode()+"_"+region.getName()+".txt";
		String fileName = "d:/regions/01.txt";
		StringBuffer text = new StringBuffer();
		text.append(region.getCode()).append(",").append(region.getParent()).append(",").append(region.getLevel()).append(",").append(region.getName()).append("\n");
		System.out.print(text);
		Files.asCharSink(new File(fileName), Charset.defaultCharset(), FileWriteMode.APPEND).write(text.toString()); 
	}

	/**
	 * 解析城市数据
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void parseCities(String href) throws MalformedURLException, IOException {
		Document document = parse("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/"+href);
		Elements elements = document.getElementsByClass("citytr");
		for(int i = 0;i < elements.size();i++) {
			Element tr = elements.get(i);
			Elements tds = tr.getElementsByTag("td");
			//<a href="22/2203.html">220300000000</a>
			Element td0 = tds.get(0);
			//<a href="22/2203.html">四平市</a>
			Element td1 = tds.get(1);
			Region region = Region.builder().level(2).name(td1.text()).code(td0.text()).parent(td0.text().substring(0, 2)+"0000000000").build();
			output(region);
			
			parseRegions(td0.getElementsByTag("a").get(0).attr("href"));
		}
	}

	/**
	 * 解析区县数据
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void parseRegions(String href) throws MalformedURLException, IOException {
		Document document = parse("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/"+href);
		Elements elements = document.getElementsByClass("countytr");
		for(int i = 0;i < elements.size();i++) {
			Element tr = elements.get(i);
			Elements tds = tr.getElementsByTag("td");
			Element td0 = tds.get(0);
			Element td1 = tds.get(1);
			Elements as = td0.getElementsByTag("a");
			if(as.isEmpty()) {
				//样式1: <td>370101000000</td><td>市辖区</td>
				Region region = Region.builder().code(td0.text()).parent(td0.text().substring(0, 4)+"00000000").level(3).name(td1.text()).build();
				output(region);
			}else {
				//样式2:<td><a href="01/370102.html">370102000000</a></td><td><a href="01/370102.html">历下区</a></td>
				String code = as.get(0).text();
				String name = td1.getElementsByTag("a").get(0).text();
				String link = as.get(0).attr("href");
				Region region = Region.builder().code(code).parent(code.substring(0, 4)+"00000000").level(3).name(name).build();
				output(region);
				parseTowns(href.substring(0, href.indexOf("/")+1)+link);
			}
			
		}
		
	}

	/**
	 * 解析镇数据
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void parseTowns(String link) throws MalformedURLException, IOException {
		Document document = parse("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/"+link);
		Elements elements = document.getElementsByClass("towntr");
		for(int i = 0;i < elements.size();i++) {
			Element tr = elements.get(i);
			Elements tds = tr.getElementsByTag("td");
			Element td0 = tds.get(0);
			Element td1 = tds.get(1);
			Elements as = td0.getElementsByTag("a");
			String code = as.get(0).text();
			Region region = Region.builder().level(4).code(code).parent(code.substring(0, 6)+"000000").name(td1.getElementsByTag("a").get(0).text()).build();
			output(region);
			parseVillages(link.substring(0, link.lastIndexOf("/")+1)+as.get(0).attr("href"));
		}
	}

	/**
	 * 解析村庄数据
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void parseVillages(String link) throws MalformedURLException, IOException {
		Document document = parse("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/"+link);
		Elements elements = document.getElementsByClass("villagetr");
		for(int i = 0;i < elements.size();i++) {
			Element tr = elements.get(i);
			Elements tds = tr.getElementsByTag("td");
			Element td0 = tds.get(0);
			Element td2 = tds.get(2);
			Region region = Region.builder().level(5).code(td0.text()).parent(td0.text().substring(0, 9)+"000").name(td2.text()).build();
			output(region);
		}
	}
}
