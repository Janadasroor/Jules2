package com.jnd.jules.model

import com.google.gson.annotations.SerializedName

data class Session(
    val name: String? = null,
    val id: String? = null,
    val prompt: String,
    val sourceContext: SourceContext,
    val title: String? = null,
    val requirePlanApproval: Boolean? = null,
    val automationMode: AutomationMode? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val state: SessionState? = null,
    val url: String? = null,
    val outputs: List<SessionOutput>? = null
)

data class SourceContext(
    val source: String,
    val githubRepoContext: GitHubRepoContext? = null
)

data class GitHubRepoContext(
    val startingBranch: String
)

enum class AutomationMode {
    AUTOMATION_MODE_UNSPECIFIED,
    AUTO_CREATE_PR
}

enum class SessionState {
    STATE_UNSPECIFIED,
    QUEUED,
    PLANNING,
    AWAITING_PLAN_APPROVAL,
    AWAITING_USER_FEEDBACK,
    IN_PROGRESS,
    PAUSED,
    FAILED,
    COMPLETED
}

data class SessionOutput(
    val pullRequest: PullRequest?
)

data class PullRequest(
    val url: String?,
    val title: String?,
    val description: String?
)

data class ListSessionsResponse(
    val sessions: List<Session>?,
    val nextPageToken: String?
)

data class SendMessageRequest(
    val prompt: String
)

data class ListSourcesResponse(
    val sources: List<Source>?,
    val nextPageToken: String?
)

data class Source(
    val name: String,
    val id: String?,
    val githubRepo: GitHubRepo?
)

data class GitHubRepo(
    val owner: String?,
    val repo: String?,
    val isPrivate: Boolean?,
    val defaultBranch: GitHubBranch?,
    val branches: List<GitHubBranch>?
)

data class GitHubBranch(
    val displayName: String?
)

data class ListActivitiesResponse(
    val activities: List<Activity>?,
    val nextPageToken: String?
)

data class Activity(
    val createTime: String?,
    val originator: String?,
    val artifacts: List<Artifact>?,
    val agentMessaged: AgentMessaged?,
    val userMessaged: UserMessaged?,
    val planGenerated: PlanGenerated?,
    val planApproved: PlanApproved?,
    val progressUpdated: ProgressUpdated?,
    val sessionCompleted: SessionCompleted?,
    val sessionFailed: SessionFailed?
)

data class Artifact(
    val changeSet: ChangeSet?,
    val media: Media?,
    val bashOutput: BashOutput?
)

data class ChangeSet(
    val source: String?,
    val gitPatch: GitPatch?
)

data class GitPatch(
    val unidiffPatch: String?,
    val baseCommitId: String?,
    val suggestedCommitMessage: String?
)

data class Media(
    val data: String?,
    val mimeType: String?
)

data class BashOutput(
    val command: String?,
    val output: String?,
    val exitCode: Int?
)

data class AgentMessaged(
    val agentMessage: String?
)

data class UserMessaged(
    val userMessage: String?
)

data class PlanGenerated(
    val plan: Plan?
)

data class Plan(
    val id: String?,
    val steps: List<PlanStep>?,
    val createTime: String?
)

data class PlanStep(
    val id: String?,
    val title: String?,
    val description: String?,
    val index: Int?
)

data class PlanApproved(
    val planId: String?
)

data class ProgressUpdated(
    val title: String?,
    val description: String?
)

class SessionCompleted

data class SessionFailed(
    val reason: String?
)
