/*******************************************************************************
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************/
package com.liferay.ide.maven.core;

import com.liferay.ide.core.IWebProject;
import com.liferay.ide.core.LiferayCore;
import com.liferay.ide.core.util.CoreUtil;
import com.liferay.ide.server.remote.AbstractRemoteServerPublisher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;

/**
 * @author Simon Jiang
 * @author Gregory Amerson
 */
public class MavenProjectRemoteServerPublisher extends AbstractRemoteServerPublisher
{
    public MavenProjectRemoteServerPublisher( IProject project )
    {
        super( project );
    }

    private String getMavenDeployGoals()
    {
        return "package war:war";
    }

    @Override
    public void processResourceDeltas(
        final IModuleResourceDelta[] deltas, ZipOutputStream zip, Map<ZipEntry, String> deleteEntries,
        final String deletePrefix, final String deltaPrefix, final boolean adjustGMTOffset )
        throws IOException, CoreException
    {
        for( final IModuleResourceDelta delta : deltas )
        {
            final IResource deltaResource = (IResource) delta.getModuleResource().getAdapter( IResource.class );
            final IProject deltaProject = deltaResource.getProject();
            final IWebProject lrproject = LiferayCore.create( IWebProject.class, deltaProject );

            if( lrproject == null || lrproject.getDefaultDocrootFolder() == null )
            {
                continue;
            }

            final IFolder webappRoot = lrproject.getDefaultDocrootFolder();
            final int deltaKind = delta.getKind();
            final IPath deltaFullPath = deltaResource.getFullPath();

            boolean deltaZip = false;
            IPath deltaPath = null;

            if( webappRoot != null && webappRoot.exists() )
            {
                final IPath containerFullPath = webappRoot.getFullPath();

                if ( containerFullPath.isPrefixOf( deltaFullPath ))
                {
                    deltaZip = true;
                    deltaPath = new Path( deltaPrefix + deltaFullPath.makeRelativeTo( containerFullPath ) );
                }
            }

            if ( deltaZip ==false && new Path("WEB-INF").isPrefixOf( delta.getModuleRelativePath() ))
            {
                final List<IFolder> folders = CoreUtil.getSourceFolders( JavaCore.create( deltaProject ) );

                for( IFolder folder : folders )
                {
                    final IPath folderPath = folder.getFullPath();

                    if ( folderPath.isPrefixOf( deltaFullPath ) )
                    {
                        deltaZip = true;
                        break;
                    }
                }
            }

            if( deltaZip == false && ( deltaKind == IModuleResourceDelta.ADDED ||
                                       deltaKind == IModuleResourceDelta.CHANGED ||
                                       deltaKind == IModuleResourceDelta.REMOVED ) )
            {
                final IPath targetPath = JavaCore.create( deltaProject ).getOutputLocation();

                deltaZip = true;
                deltaPath = new Path( "WEB-INF/classes" ).append( deltaFullPath.makeRelativeTo( targetPath ) );
            }

            if ( deltaZip )
            {
                if( deltaKind == IModuleResourceDelta.ADDED || deltaKind == IModuleResourceDelta.CHANGED )
                {
                    addToZip( deltaPath, deltaResource, zip, adjustGMTOffset );
                }
                else if( deltaKind == IModuleResourceDelta.REMOVED )
                {
                    addRemoveProps( deltaPath, deltaResource, zip, deleteEntries, deletePrefix );
                }
                else if( deltaKind == IModuleResourceDelta.NO_CHANGE )
                {
                    final IModuleResourceDelta[] children = delta.getAffectedChildren();
                    processResourceDeltas( children, zip, deleteEntries, deletePrefix, deltaPrefix, adjustGMTOffset );
                }
            }
        }
    }

    public IPath publishModuleFull( IProgressMonitor monitor ) throws CoreException
    {
        IPath retval = null;

        final MavenProjectBuilder mavenProjectBuilder = new MavenProjectBuilder( this.getProject() );

        if( mavenProjectBuilder.runMavenGoal( getProject(), getMavenDeployGoals(), monitor ) )
        {
            final IMavenProjectFacade projectFacade = MavenUtil.getProjectFacade( getProject(), monitor );
            final MavenProject mavenProject = projectFacade.getMavenProject( monitor );
            final String targetFolder = mavenProject.getBuild().getDirectory();
            final String targetWar = mavenProject.getBuild().getFinalName() + "." + mavenProject.getPackaging();

            retval = new Path( targetFolder ).append( targetWar );
        }

        return retval;
    }
}
