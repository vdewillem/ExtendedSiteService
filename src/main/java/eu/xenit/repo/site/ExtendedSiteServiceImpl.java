package eu.xenit.repo.site;

import eu.xenit.repo.site.groups.SiteGroup;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExtendedSiteServiceImpl extends SiteServiceImpl
{
    private static final Log logger = LogFactory.getLog(ExtendedSiteServiceImpl.class);

    private List<SiteGroup> siteGroups = new ArrayList<SiteGroup>();

    private PermissionService permissionService;

    public void setSiteGroups(List<SiteGroup> siteGroups) {
        this.siteGroups.addAll(siteGroups);
    }

    public List<SiteGroup> getSiteGroups() {
        return siteGroups;
    }

    public void setPermissionService2(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean isMember(String shortName, String authorityName) {
        if(logger.isDebugEnabled())
            logger.debug(String.format("isMember('%s','%s')",shortName,authorityName));

        if(super.isMember(shortName, authorityName))
            return true;

        for(SiteGroup siteGroup : this.siteGroups){
            if(siteGroup.isRelevant(shortName, null) && siteGroup.isMember(authorityName)){
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size, boolean collapseGroups) {
        if(logger.isDebugEnabled())
            logger.debug(String.format("listMembers(%s,%s)",shortName,roleFilter));
        Map<String, String> members =  super.listMembers(shortName, nameFilter, roleFilter, size, collapseGroups);

        for(SiteGroup siteGroup : this.siteGroups){
            if(siteGroup.isRelevant(shortName, roleFilter)){
                for(String user : siteGroup.getMembers())
                    members.put(user,roleFilter);
            }
        }

        return members;
    }

    public void expand(NodeRef nodeRef)
    {
        SiteInfo siteInfo = this.getSite(nodeRef);
        if(siteInfo == null){
            logger.warn(String.format("'%s' is not a site", nodeRef));
            return;
        }

        Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(nodeRef);
        for(AccessPermission permission : permissions)
        {
            for(SiteGroup siteGroup : this.getSiteGroups()){
                AccessPermission newPermission = expand(permission, siteInfo.getShortName(), siteGroup);
                if(newPermission != null)
                    this.permissionService.setPermission(nodeRef, newPermission.getAuthority(), newPermission.getPermission(), true);
            }
        }
    }

    public static AccessPermission expand(AccessPermission p, String site, SiteGroup siteGroup) {
        String role = siteGroup.getActualRole(p,site);
        if(role == null)
            return null;

        return new AccessPermissionImpl(p.getPermission(), p.getAccessStatus(), siteGroup.getName(),0);
    }
}