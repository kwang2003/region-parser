package com.pachiraframework.region;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

import lombok.Data;

/**
 * @author wangxuzheng
 *
 */
public class RegionSummary {
	public static void main(String[] args) throws IOException {
		new RegionSummary().execute();
	}
	
	private void execute() throws IOException {
		Map<String, RegionNode> nodes = Maps.newHashMap();
		List<RegionNode> root = Lists.newArrayList();
		Files.asCharSource(new File("D:\\regions\\05.txt"), Charset.forName("UTF-8")).readLines(new LineProcessor<String>() {
			@Override
			public boolean processLine(String line) throws IOException {
				if(Strings.isNullOrEmpty(line)) {
					return false;
				}
				
				RegionNode node = buildRegionNode(line);
				nodes.put(node.getCode(), node);
				if(node.getLevel() == 1) {
					root.add(node);
				}
				
				String pid = node.getPid();
				RegionNode parent = nodes.get(pid);
				if(pid == null) {
					System.out.println("#################");
				}
				if(parent.getLevel()+1==node.getLevel()) {
					parent.addChild(node);
				}
				return true;
			}

			@Override
			public String getResult() {
				return null;
			}
			
		});
		
		
		// 遍历数量
		List<OutData> result = Lists.newArrayList();
		for(RegionNode p : root) {// 省
			for(RegionNode c : p.getChildren()) {//市
				OutData out = new OutData();
				out.setCnam(c.getName());
				out.setPname(p.getName());
//				out.setRname(r.getName());
				result.add(out);
				for(RegionNode r : c.getChildren()) {//区
					for(RegionNode t : r.getChildren()) {//镇
						for(RegionNode v : t.getChildren()) {//村
							out.setSum(out.getSum()+1);
						}
					}
				}
			}
		}
		
		// 输出结果
		for(OutData out : result) {
			System.out.println(out.getPname()+"\t"+out.getCnam()/*+"\t"+out.getRname()*/+"\t"+out.getSum());
		}
	}
	
	private RegionNode buildRegionNode(String line) {
		String[] strs = line.split(",");
		RegionNode node = new RegionNode();
		node.setCode(strs[0]);
		node.setLevel(Integer.valueOf(strs[2]));
		node.setName(strs[3]);
		node.setPid(strs[1]);
		return node;
	}
	
	@Data
	private static class OutData{
		private String pname;
		private String cnam;
		private String rname;
		private int sum;
	}
	
	
	@Data
	private static class RegionNode{
		private String code;
		private String name;
		private String pid;
		private int level;
		private List<RegionNode> children = Lists.newArrayList();
		public RegionNode addChild(RegionNode child) {
			this.children.add(child);
			return this;
		}
	}
}
