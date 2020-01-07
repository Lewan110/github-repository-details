package com.adrian.task;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public class TaskApplicationIntegrationTests {

    private final static String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    private final static String CONTENT_TYPE_HEADER = "Content-Type";

    @Autowired
    MockMvc mockMvc;

    @Value("${github.path}")
    private String path;

    @Before
    public void init() {
        listAllStubMappings().getMappings().forEach(WireMock::removeStub);
    }

    @After
    public void clean() {
        listAllStubMappings().getMappings().forEach(WireMock::removeStub);
    }

    @Test
    public void whenValidOwnerAndRepositoryName_thenShouldReturnValidResponse() throws Exception {

        //given
        String owner = "spring-projects";
        String repositoryName = "spring-framework";
        String requestPath = path + "/" + owner + "/" + repositoryName;

        String fullName = owner + "/" + repositoryName;
        String description = "Spring Framework";
        String cloneUrl = "https://github.com/" + fullName + ".git";
        String stars = "34538";
        String createdAt = "2010-12-08T04:04:45Z";
        String githubApiResponse = createSimplifiedGithubApiResponse(fullName, description, cloneUrl, stars, createdAt);

        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/" + requestPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(githubApiResponse)
                        .withHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE)));

        //when
        ResultActions response = mockMvc.perform(get("/repositories/" + owner + "/" + repositoryName)
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE))
                .andDo(print());

        //then
        response.andExpect(status().isOk());
        response.andExpect(jsonPath("$.fullName").value(fullName));
        response.andExpect(jsonPath("$.description").value(description));
        response.andExpect(jsonPath("$.cloneUrl").value(cloneUrl));
        response.andExpect(jsonPath("$.stars").value(stars));
        response.andExpect(jsonPath("$.createdAt").value(createdAt));

    }

    @Test
    public void whenRepositoryOrUserDoesntExists_thenShouldReturn404ResponseWithValidErrorMessage() throws Exception {

        //given
        String owner = "random";
        String repositoryName = "some_repo";
        String requestPath = path + "/" + owner + "/" + repositoryName;

        String githubErrorMessage = "{\n" +
                "  \"message\": \"Not Found\",\n" +
                "  \"documentation_url\": \"https://developer.github.com/v3/repos/#get\"\n" +
                "}";
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/" + requestPath))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(githubErrorMessage)
                        .withHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE)));

        //when
        ResultActions response = mockMvc.perform(get("/repositories/" + owner + "/" + repositoryName)
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE))
                .andDo(print());

        //then
        response.andExpect(status().isNotFound());
        response.andExpect(jsonPath("$.details").value("Cannot find repository with given params"));
    }

    @Test
    public void whenRequestLimitExceeded_thenShouldReturn403ResponseWithValidErrorMessage() throws Exception {

        //given
        String owner = "random";
        String repositoryName = "some_repo";
        String requestPath = path + "/" + owner + "/" + repositoryName;

        String githubErrorMessage = "{\n" +
                "  \"message\": \"API rate limit exceeded for ip.ip.ip.ip. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)\",\n" +
                "  \"documentation_url\": \"https://developer.github.com/v3/#rate-limiting\"\n" +
                "}";
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/" + requestPath))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withBody(githubErrorMessage)
                        .withHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE)));

        //when
        ResultActions response = mockMvc.perform(get("/repositories/" + owner + "/" + repositoryName)
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE))
                .andDo(print());

        //then
        response.andExpect(status().isForbidden());
        response.andExpect(jsonPath("$.details").value("Sorry, your request cannot be processed"));
    }

    private String createSimplifiedGithubApiResponse(String fullName, String description, String cloneUrl, String stars, String createdAt) {
        return "{\n" +
                "  \"id\": 1148753,\n" +
                "  \"node_id\": \"MDEwOlJlcG9zaXRvcnkxMTQ4NzUz\",\n" +
                "  \"name\": \"spring-framework\",\n" +
                "  \"full_name\": \"" + fullName + "\",\n" +
                "  \"private\": false,\n" +
                "  \"owner\": {\n" +
                "    \"login\": \"spring-projects\",\n" +
                "    \"id\": 317776,\n" +
                "    \"node_id\": \"MDEyOk9yZ2FuaXphdGlvbjMxNzc3Ng==\",\n" +
                "    \"avatar_url\": \"https://avatars0.githubusercontent.com/u/317776?v=4\",\n" +
                "    \"type\": \"Organization\",\n" +
                "    \"site_admin\": false\n" +
                "  },\n" +
                "  \"html_url\": \"https://github.com/spring-projects/spring-framework\",\n" +
                "  \"description\": \"" + description + "\",\n" +
                "  \"fork\": false,\n" +
                "  \"created_at\": \"\",\n" +
                "  \"created_at\": \"" + createdAt + "\",\n" +
                "  \"updated_at\": \"2020-01-02T10:02:45Z\",\n" +
                "  \"pushed_at\": \"2019-12-31T17:58:50Z\",\n" +
                "  \"git_url\": \"git://github.com/spring-projects/spring-framework.git\",\n" +
                "  \"ssh_url\": \"git@github.com:spring-projects/spring-framework.git\",\n" +
                "  \"clone_url\": \"" + cloneUrl + "\",\n" +
                "  \"svn_url\": \"https://github.com/spring-projects/spring-framework\",\n" +
                "  \"homepage\": \"https://spring.io/projects/spring-framework\",\n" +
                "  \"size\": 152116,\n" +
                "  \"stargazers_count\":" + stars + ",\n " +
                "  \"open_issues_count\": 1071,\n" +
                "  \"license\": {\n" +
                "    \"key\": \"apache-2.0\",\n" +
                "    \"name\": \"Apache License 2.0\",\n" +
                "    \"spdx_id\": \"Apache-2.0\",\n" +
                "    \"url\": \"https://api.github.com/licenses/apache-2.0\",\n" +
                "    \"node_id\": \"MDc6TGljZW5zZTI=\"\n" +
                "  },\n" +
                "  \"forks\": 22850,\n" +
                "  \"open_issues\": 1071,\n" +
                "  \"watchers\": 34538,\n" +
                "  \"default_branch\": \"master\",\n" +
                "  \"temp_clone_token\": null,\n" +
                "  \"organization\": {\n" +
                "    \"login\": \"spring-projects\",\n" +
                "    \"type\": \"Organization\",\n" +
                "    \"site_admin\": false\n" +
                "  },\n" +
                "  \"network_count\": 22850,\n" +
                "  \"subscribers_count\": 3437\n" +
                "}";
    }
}
