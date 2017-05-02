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

package com.liferay.ide.project.core.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
/**
 * @author Joye Luo
 */
public class RepositoryUtil
{

    public static Repository createLocalRepository( String repoPath )
        throws IOException, GitAPIException, GitAPIException
    {
        Repository repo = null;
        File gitFile = new File( repoPath, ".git" );
        if( gitFile.exists() )
        {
            FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
            repoBuilder.setWorkTree( new File( repoPath ) );
            repo = repoBuilder.build();

            try(Git git = new Git( repo ))
            {
                git.branchCreate().setName( "code-upgrade" ).call();
                git.checkout().setName( "code-upgrade" ).call();
            }
        }
        else
        {
            repo = FileRepositoryBuilder.create( new File( repoPath, ".git" ) );
            repo.create();

            try(Git git = new Git( repo ))
            {
                git.add().addFilepattern( "." ).call();
                git.commit().setAll( true ).setAuthor( "liferay", "liferay@example.com" ).setMessage(
                    "create a local repository" ).call();

                git.branchCreate().setName( "code-upgrade" ).call();
                git.checkout().setName( "code-upgrade" ).call();
            }
        }
        return repo;
    }

    public static void commmitAllChanges( String commitInfo, String repoPath ) throws Exception
    {
        FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
        repoBuilder.setWorkTree( new File( repoPath ) );
        Repository repo = repoBuilder.build();

        try(Git git = new Git( repo ))
        {
            if( isModified( git ) )
            {
                git.add().addFilepattern( "." ).call();
                git.commit().setAll( true ).setAuthor( "liferay", "liferay@example.com" ).setMessage(
                    commitInfo ).call();
            }
            else
            {
                System.out.println( "there is no changes need to commit" );
            }
        }
    }

    public static boolean isModified( Git git ) throws Exception
    {
        org.eclipse.jgit.api.Status status = git.status().call();

        if( status.hasUncommittedChanges() || !status.getUntracked().isEmpty() )
        {
            return true;
        }
        return false;
    }
}
