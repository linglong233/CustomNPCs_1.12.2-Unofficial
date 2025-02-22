package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.util.AdditionalMethods;

public class MetodData {

	String ifc = "";
	private String returnTypeName;
	public String name;
	private List<ParameterData> parameters;
	private String comment;
	private boolean isDeprecated;

	public MetodData(Class<?> ret, String name, String comment, ParameterData ... parameters) {
		this.returnTypeName = "c" + ret.getSimpleName();
		if (ret.isInterface()) { this.returnTypeName = "9" + ret.getSimpleName(); }
		else if (ret == boolean.class || ret == byte.class || ret == short.class || ret == int.class || ret == float.class || ret == double.class || ret == long.class || ret == String.class) { this.returnTypeName = "e" + ret.getSimpleName(); }
		else if (ret == Void.class) { this.returnTypeName = "8void"; }
		this.name = name;
		this.comment = comment;
		this.parameters = Lists.<ParameterData>newArrayList();
		for (ParameterData pd : parameters) { this.parameters.add(pd); }
		this.isDeprecated = false;
	}
	
	public String getText() {
		char chr = Character.toChars(0x00A7)[0];
		String text = "";
		if (!this.parameters.isEmpty()) {
			for (ParameterData pd : this.parameters) {
				if (text.isEmpty()) { text = "("+pd.typename; }
				else { text += ", "+pd.typename; }
			}
			text += ")";
		} else { text = "()"; }
		String total = chr + this.returnTypeName +chr+"f " + this.name + text + chr + "f;";
		if (this.isDeprecated) {
			total = chr + "8" + AdditionalMethods.deleteColor(total);
		}
		return total;
	}
	
	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		comment.add(((char) 167)+"bMetod: "+((char) 167)+"f"+this.name+((char) 167)+"b; Interface: "+((char) 167)+"f"+this.ifc);
		if (this.isDeprecated) {
			comment.add(new TextComponentTranslation("metod.deprecated").getFormattedText());
		}
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) {
				comment.add(t);
			}
		}
		else { comment.add(tr); }
		if (!this.parameters.isEmpty()) {
			for (ParameterData pd : this.parameters) {
				comment.addAll(pd.getComment());
			}
		}
		return comment;
	}
	
	public MetodData setDeprecated() {
		this.isDeprecated = true;
		return this;
	}
}
