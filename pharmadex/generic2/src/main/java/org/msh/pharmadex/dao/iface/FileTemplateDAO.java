package org.msh.pharmadex.dao.iface;

import java.util.List;

import org.msh.pharmadex.domain.FileTemplate;
import org.msh.pharmadex.domain.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Author: muskiv
 */
public interface FileTemplateDAO extends JpaRepository<FileTemplate, Long> {
	List<FileTemplate> findByTemplateType(TemplateType templateType);
}
