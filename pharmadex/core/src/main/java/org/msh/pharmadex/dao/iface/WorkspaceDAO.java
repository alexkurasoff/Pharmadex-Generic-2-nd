package org.msh.pharmadex.dao.iface;

import org.msh.pharmadex.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/11/12
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */

public interface WorkspaceDAO extends JpaRepository<Workspace, Long> {


}
