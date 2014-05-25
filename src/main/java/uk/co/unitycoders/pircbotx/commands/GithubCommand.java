package uk.co.unitycoders.pircbotx.commands;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import uk.co.unitycoders.pircbotx.commandprocessor.Command;
import uk.co.unitycoders.pircbotx.commandprocessor.Message;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by webpigeon on 25/05/14.
 */
public class GithubCommand {
    private static final String SEPARATOR = " || ";
    private GitHubClient client;


    public GithubCommand() {
        this.client = new GitHubClient();
    }

    @Command("repos")
    public void showUserRepos(Message message) throws IOException {
        String[] args = message.getMessage().split(" ");

        if (args.length > 3) {
            throw new IllegalArgumentException("Usage: github repos [username]");
        }

            String username = args[2];
            RepositoryService service = new RepositoryService();
            List<Repository> repos = service.getRepositories(username);

            if (repos.isEmpty()) {
                message.respond("user has no repos");
            } else{
                String[] repoNames = new String[repos.size()];
                for (int i=0; i<repos.size(); i++) {
                    repoNames[i] = repos.get(i).getName();
                }

                message.respond(username+"'s repos are: "+Arrays.toString(repoNames));
            }
    }

    @Command("issue")
    public void showIssueDetails(Message message) throws IOException {
        String[] args = message.getMessage().split(" ");

        if (args.length > 5) {
            throw new IllegalArgumentException("Usage: github issue [user] [repo] [number]");
        }

            IssueService service = new IssueService();
            Issue issue = service.getIssue(args[2], args[3], args[4]);

            String issueDetails = "#"+issue.getNumber() + issue.getTitle() + SEPARATOR + issue.getState() + SEPARATOR + issue.getUrl();
            message.respond(issueDetails);


    }
}
