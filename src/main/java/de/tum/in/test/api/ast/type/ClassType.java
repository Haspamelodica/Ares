package de.tum.in.test.api.ast.type;

import java.util.Map;

import com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;

import static de.tum.in.test.api.localization.Messages.localized;

@API(status = Status.MAINTAINED)
public enum ClassType implements Type {
	/**
	 * The local class type (statements of the form: "class" + class name + "{" + "}")
	 */
	CLASS(Map.of(localized("ast.enum.class_type.class"), LocalClassDeclarationStmt.class)), //$NON-NLS-1$

	/**
	 * The local record type (statements of the form: "record" + record name + "(" + record attributes + ")" + "{" + "}")
	 */
	RECORD(Map.of(localized("ast.enum.class_type.record"), LocalRecordDeclarationStmt.class)); //$NON-NLS-1$

	private final Map<String, Class<? extends Node>> nodeNameNodeMap;

	ClassType(Map<String, Class<? extends Node>> nodeNameNodeMap) {
		this.nodeNameNodeMap = nodeNameNodeMap;
	}

	@Override
	public Map<String, Class<? extends Node>> getNodeNameNodeMap() {
		return nodeNameNodeMap;
	}
}
