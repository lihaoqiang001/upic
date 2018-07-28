package com.xyz.keshe.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.xyz.keshe.service.impl.PicServiceImpl;
import com.xyz.keshe.util.OSSClientConstants;

@Controller
public class PicController {
	
	@Autowired
	private PicServiceImpl picService;
	
	private SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private Random rand = new Random();
	
	/**
	 * 上传图片
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public List<String> upload(@RequestParam("pic")MultipartFile file,@RequestParam("mId")int mId, HttpServletRequest request) {
		
		List<String> list = new ArrayList<String>();
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
        file = multipartRequest.getFile("pic"); 
		if (!file.isEmpty()) {
			String mime = file.getContentType();
			String fname = file.getOriginalFilename();
			ServletContext sc = request.getServletContext();
			String base = sc.getRealPath("picRep/");
			String newname = randomfilename(fname, mId);
			File target = new File(base, newname);

			try {
				file.transferTo(target);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String path = picService.upload(target);
			insert(mId, path);
			list.add(path);
			return list;
		}
		list.add("上传失败");
		return list;
	}
	

	/**
	 * 文件重命名
	 * @param oname
	 * @param id
	 */
	public String randomfilename(String oname, int id) {
		String extname = "";
		if (null != oname && oname.toLowerCase().lastIndexOf(".") != -1) {
			extname = oname.toLowerCase().substring(oname.toLowerCase().lastIndexOf("."));
		}
		String filename = "";
		switch(id) {
		case 1:
			filename="精选收藏夹_明星设计师";break;
		case 2:
			filename="精选收藏夹_世界杯";break;
		case 3:
			filename="精选收藏夹_毕业季";break;
		case 4:
			filename="商用海报_旅游海报";break;
		case 5:
			filename="商用海报_美食海报";break;
		case 6:
			filename="商用海报_招聘海报";break;
		case 7:
			filename="手机用图_表情包配图";break;
		case 8:
			filename="手机用图_企业宣传";break;
		case 9:
			filename="插画绘图_夏天插画";break;
		case 10:
			filename="插画绘图_星空插画";break;
		}
		
		if(id<10)
			return "0"+id + sf.format(new Date()) + "_" + rand.nextInt(1000)+filename + extname;
		else
			return id + sf.format(new Date()) + "_" + rand.nextInt(1000)+filename + extname;
	}
	
	public int insert(int mId,String pUrl) {
		return picService.insert(mId, pUrl);
	}
	
	
	/**
	 * 搜索图片
	 */
	@RequestMapping("/search")
	@ResponseBody
	public List<String> searchPic(@RequestParam("pName")String pName, HttpServletRequest request) {
		return picService.search(pName);
	}
	
	
	/**
	 * 下载榜
	 */
	@RequestMapping("/sort")
	@ResponseBody
	public List<String> sort(HttpServletRequest request){
		return picService.sort();
	}
	
	
	/**
	 * 从阿里云下载文件 （以附件形式下载）
	 */
	@ResponseBody
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public void downLoadFile(HttpServletRequest request, HttpServletResponse response){
		try {
			String fileid = request.getParameter("8521114C5613F1E46AE9404C55134D14").toString();//从前台获取当前下载文件的id值（每个上传到阿里云的文件都会有一个独一无二的id值）
			String filename =request.getParameter("pic/0520180713111946814_842商用海报_美食海报.jpg").toString();//从前台获取要下载文件的文件名
			int i=filename.lastIndexOf("\\");
			filename=filename.substring(i+1);

			OSSClient ossClient  = new OSSClient(OSSClientConstants.ENDPOINT, OSSClientConstants.ACCESS_KEY_ID, OSSClientConstants.ACCESS_KEY_SECRET);
			//获取fileid对应的阿里云上的文件对象
			OSSObject ossObject = ossClient.getObject(OSSClientConstants.BACKET_NAME, fileid);//bucketName需要自己设置
			
			// 读去Object内容  返回
			BufferedInputStream in=new BufferedInputStream(ossObject.getObjectContent());
			//System.out.println(ossObject.getObjectContent().toString());
			
			
			BufferedOutputStream out=new BufferedOutputStream(response.getOutputStream());
			//通知浏览器以附件形式下载
			response.setHeader("Content-Disposition","attachment;filename="+java.net.URLEncoder.encode(filename,"utf-8"));
			//BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(new File("f:\\a.txt")));
			
			
			byte[] car=new byte[1024];
			int L=0;
			while((L=in.read(car))!=-1){
				out.write(car, 0,L);
				
			}
			if(out!=null){
				out.flush();
				out.close();
			}
			if(in!=null){
				in.close();
			}
			
			ossClient.shutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
