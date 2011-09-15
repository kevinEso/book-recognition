package org.k53ca;

import java.net.*;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Client {
	
	static void writeKey(DataOutputStream dos, KeyPoint k) {
		try {
			dos.writeFloat(k.angle);
			dos.writeInt(k.classId);
			dos.writeInt(k.octave);
			dos.writeFloat(k.x);
			dos.writeFloat(k.y);
			dos.writeFloat(k.response);
			dos.writeFloat(k.size);
		}
		catch(Exception exc) {
			
		}
	}
	
	static void writeMat(DataOutputStream dos, Mat m) {
		try {
			dos.writeInt(m.rows);
			dos.writeInt(m.cols);
			int size = m.rows*m.cols;
			for(int i = 0; i < size; ++i) {
				dos.writeFloat(m.data[i]);
			}
		} 
		catch(Exception exc) {
			
		}
	}
	
	static void sendMatch(DataOutputStream dos, String filename) {
		try {
			Book book = DataIO.createBook(filename);
			dos.writeBytes("MATCH ");
			int size = book.keys.size();
//			int dataSize = size*book.keys.firstElement().getSize()+book.dess.getSize()+1;
//			dos.writeInt(dataSize);
			dos.writeInt(size);
			Iterator<KeyPoint> it = book.keys.iterator();
			for(int i = 0; i < size; ++i) {
				writeKey(dos, it.next());
			}
			writeMat(dos, book.dess);
			dos.writeBytes("\n");
			//dos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void sendMatchString(OutputStream out, String imgName) {
		try {
			BufferedImage img = ImageIO.read(new File(imgName));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write("MATCH " + ConvertValue.bitmapToBase64String(img) + "\n");
			bw.flush();
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	static void getImage(String xml) throws ParserConfigurationException, SAXException, IOException {
		StringReader sr = new StringReader(xml);
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.parse(is);
		NodeList nl = dom.getElementsByTagName("image");
		for(int i = 0; i < nl.getLength(); ++i) {
			BufferedImage img = ConvertValue.base64StringToBitmap(nl.item(i).getFirstChild().getNodeValue());
			ImageIO.write(img, "JPG", new File(i+".jpg"));
		}
	}
	
	public static void main(String args[]) throws ParserConfigurationException, SAXException {
		Socket skt;
		DataOutputStream dos;
		//for(int i = 0; i < 100; ++i)
			try {
				skt = new Socket("119.15.161.26", 8080);
				long start = System.currentTimeMillis();
				//skt = new Socket("localhost", 8080);
				dos = new DataOutputStream(skt.getOutputStream());
				Scanner input = new Scanner(skt.getInputStream());
				//dos.writeBytes("SIMILAR 156 \n");
				sendMatchString(dos, "pics/book (" + 9 + ").jpg");
				skt.shutdownOutput();
				System.out.println("Done send");
				String response = null;
				while(input.hasNext()){
					response = input.nextLine();
					System.out.println("Response " + response);
					response = "<bb>" + response + "</bb>";
				}
				System.out.println("Response time: " + (System.currentTimeMillis()-start));
				getImage(response);
				dos.close();
				skt.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
