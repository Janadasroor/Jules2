package com.jnd.jules.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jnd.jules.model.Session
import com.jnd.jules.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.jnd.jules.model.Source

class SessionViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    private val _sources = MutableStateFlow<List<Source>>(emptyList())
    val sources: StateFlow<List<Source>> = _sources.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchSessions() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = api.listSessions()
                _sessions.value = response.sessions ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchSources() {
        viewModelScope.launch {
            try {
                 val response = api.listSources()
                 _sources.value = response.sources ?: emptyList()
            } catch (e: Exception) {
                // Log error or handle silently for now as this might be called in background
                e.printStackTrace()
            }
        }
    }

    private val _selectedSession = MutableStateFlow<Session?>(null)
    val selectedSession: StateFlow<Session?> = _selectedSession.asStateFlow()

    fun getSession(id: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val session = api.getSession(id)
                _selectedSession.value = session
                // Fetch activities for the session
                val sessionName = session.name ?: "sessions/${session.id}"
                val activitiesResponse = api.listActivities(sessionName)
                _activities.value = activitiesResponse.activities ?: emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    
    private val _activities = MutableStateFlow<List<com.jnd.jules.model.Activity>>(emptyList())
    val activities: StateFlow<List<com.jnd.jules.model.Activity>> = _activities.asStateFlow()

    fun clearSelectedSession() {
        _selectedSession.value = null
        _activities.value = emptyList()
    }

    fun createSession(prompt: String, source: com.jnd.jules.model.Source) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val repoContext = source.githubRepo?.defaultBranch?.displayName?.let { branchName ->
                    com.jnd.jules.model.GitHubRepoContext(startingBranch = branchName)
                }
                
                val sourceContext = com.jnd.jules.model.SourceContext(
                    source = source.name,
                    githubRepoContext = repoContext
                )
                
                val session = Session(
                    prompt = prompt,
                    sourceContext = sourceContext
                )
                api.createSession(session)
                // Refresh list
                fetchSessions()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendMessage(prompt: String) {
        val currentSession = _selectedSession.value ?: return
        val sessionName = currentSession.name ?: "sessions/${currentSession.id}"
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                api.sendMessage(
                     sessionName = sessionName,
                     request = com.jnd.jules.model.SendMessageRequest(prompt)
                )
                // Refresh session details
                currentSession.id?.let { getSession(it) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun approvePlan() {
        val currentSession = _selectedSession.value ?: return
        val sessionName = currentSession.name ?: "sessions/${currentSession.id}"
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                api.approvePlan(sessionName)
                // Refresh session details to see status change
                currentSession.id?.let { getSession(it) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // Methods added for API completeness, though not currently used in the UI flow
    fun getSource(name: String, onResult: (Source?) -> Unit) {
        viewModelScope.launch {
            try {
                val source = api.getSource(name)
                onResult(source)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun getActivity(name: String, onResult: (com.jnd.jules.model.Activity?) -> Unit) {
        viewModelScope.launch {
             try {
                val activity = api.getActivity(name)
                onResult(activity)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
