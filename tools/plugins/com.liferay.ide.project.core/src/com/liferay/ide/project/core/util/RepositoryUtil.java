
package com.liferay.ide.project.core.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
        }
        else
        {
            repo = FileRepositoryBuilder.create( gitFile );
            repo.create();

            Git git = new Git( repo );
            git.add().addFilepattern( "." ).call();
            git.commit().setAll( true ).setAuthor( "liferay", "liferay@example.com" ).setMessage(
                "Create a local repository for code upgrade projects" ).call();
            git.close();
        }
        return repo;
    }

    public static void commmitAllChanges( String commitInfo, String repoPath )
        throws IOException, NoFilepatternException, GitAPIException
    {
        FileRepositoryBuilder repoBuilder = new FileRepositoryBuilder();
        repoBuilder.setWorkTree( new File( repoPath ) );
        Repository repo = repoBuilder.build();
        Git git = new Git( repo );

        git.add().addFilepattern( "." ).call();
        git.commit().setAll( true ).setAuthor( "liferay", "liferay@example.com" ).setMessage( commitInfo ).call();
        git.close();
    }
}
