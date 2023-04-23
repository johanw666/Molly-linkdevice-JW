package org.thoughtcrime.securesms.components.settings.app.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.BackupUtil
import org.thoughtcrime.securesms.util.ConversationUtil
import org.thoughtcrime.securesms.util.ThrottledDebouncer
import org.thoughtcrime.securesms.util.livedata.Store
// JW: added imports
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.thoughtcrime.securesms.util.UriUtils

class ChatsSettingsViewModel @JvmOverloads constructor(
  private val repository: ChatsSettingsRepository = ChatsSettingsRepository(),
) : ViewModel() {

  private val refreshDebouncer = ThrottledDebouncer(500L)
  private val disposables = CompositeDisposable()

  private val store: Store<ChatsSettingsState> = Store(
    ChatsSettingsState(
      generateLinkPreviews = SignalStore.settings().isLinkPreviewsEnabled,
      useAddressBook = SignalStore.settings().isPreferSystemContactPhotos,
      keepMutedChatsArchived = SignalStore.settings().shouldKeepMutedChatsArchived(),
      useSystemEmoji = SignalStore.settings().isPreferSystemEmoji,
      enterKeySends = SignalStore.settings().isEnterKeySends,
      chatBackupsEnabled = SignalStore.settings().isBackupEnabled && BackupUtil.canUserAccessBackupDirectory(ApplicationDependencies.getApplication()),
      // JW: added
      chatBackupsLocation = TextSecurePreferences.isBackupLocationRemovable(ApplicationDependencies.getApplication()),
      chatBackupsLocationApi30 = UriUtils.getFullPathFromTreeUri(ApplicationDependencies.getApplication(), SignalStore.settings().signalBackupDirectory),
      chatBackupZipfile = TextSecurePreferences.isRawBackupInZipfile(ApplicationDependencies.getApplication()),
      chatBackupZipfilePlain = TextSecurePreferences.isPlainBackupInZipfile(ApplicationDependencies.getApplication()),
      keepViewOnceMessages = TextSecurePreferences.isKeepViewOnceMessages(ApplicationDependencies.getApplication()),
      ignoreRemoteDelete = TextSecurePreferences.isIgnoreRemoteDelete(ApplicationDependencies.getApplication()),
      deleteMediaOnly = TextSecurePreferences.isDeleteMediaOnly(ApplicationDependencies.getApplication()),
      whoCanAddYouToGroups = TextSecurePreferences.whoCanAddYouToGroups(ApplicationDependencies.getApplication())
    )
  )

  val state: LiveData<ChatsSettingsState> = store.stateLiveData

  override fun onCleared() {
    disposables.clear()
  }

  fun setGenerateLinkPreviewsEnabled(enabled: Boolean) {
    if (SignalStore.account().isLinkedDevice) {
      return
    }
    store.update { it.copy(generateLinkPreviews = enabled) }
    SignalStore.settings().isLinkPreviewsEnabled = enabled
    repository.syncLinkPreviewsState()
  }

  fun setUseAddressBook(enabled: Boolean) {
    store.update { it.copy(useAddressBook = enabled) }
    refreshDebouncer.publish { ConversationUtil.refreshRecipientShortcuts() }
    SignalStore.settings().isPreferSystemContactPhotos = enabled
    repository.syncPreferSystemContactPhotos()
  }

  fun setKeepMutedChatsArchived(enabled: Boolean) {
    store.update { it.copy(keepMutedChatsArchived = enabled) }
    SignalStore.settings().setKeepMutedChatsArchived(enabled)
    repository.syncKeepMutedChatsArchivedState()
  }

  fun setUseSystemEmoji(enabled: Boolean) {
    store.update { it.copy(useSystemEmoji = enabled) }
    SignalStore.settings().isPreferSystemEmoji = enabled
  }

  fun setEnterKeySends(enabled: Boolean) {
    store.update { it.copy(enterKeySends = enabled) }
    SignalStore.settings().isEnterKeySends = enabled
  }

  fun refresh() {
    val backupsEnabled = SignalStore.settings().isBackupEnabled && BackupUtil.canUserAccessBackupDirectory(ApplicationDependencies.getApplication())
    if (store.state.chatBackupsEnabled != backupsEnabled) {
      store.update { it.copy(chatBackupsEnabled = backupsEnabled) }
    }
    // JW: added. This is required to update the UI for settings that are not in the
    // Signal store but in the shared preferences.
    store.update { getState().copy() }
  }

  // JW: added
  fun setChatBackupLocation(enabled: Boolean) {
    TextSecurePreferences.setBackupLocationRemovable(ApplicationDependencies.getApplication(), enabled)
    TextSecurePreferences.setBackupLocationChanged(ApplicationDependencies.getApplication(), true) // Used in BackupUtil.getAllBackupsNewestFirst()
    refresh()
  }

  // JW: added
  fun setChatBackupLocationApi30(value: String) {
    refresh()
  }

  // JW: added
  fun setChatBackupZipfile(enabled: Boolean) {
    TextSecurePreferences.setRawBackupZipfile(ApplicationDependencies.getApplication(), enabled)
    refresh()
  }

  // JW: added
  fun setChatBackupZipfilePlain(enabled: Boolean) {
    TextSecurePreferences.setPlainBackupZipfile(ApplicationDependencies.getApplication(), enabled)
    refresh()
  }

  // JW: added
  fun keepViewOnceMessages(enabled: Boolean) {
    TextSecurePreferences.setKeepViewOnceMessages(ApplicationDependencies.getApplication(), enabled)
    refresh()
  }

  // JW: added
  fun ignoreRemoteDelete(enabled: Boolean) {
    TextSecurePreferences.setIgnoreRemoteDelete(ApplicationDependencies.getApplication(), enabled)
    refresh()
  }

  // JW: added
  fun deleteMediaOnly(enabled: Boolean) {
    TextSecurePreferences.setDeleteMediaOnly(ApplicationDependencies.getApplication(), enabled)
    refresh()
  }

  // JW: added
  fun setWhoCanAddYouToGroups(adder: String) {
    TextSecurePreferences.setWhoCanAddYouToGroups(ApplicationDependencies.getApplication(), adder)
    refresh()
  }

  // JW: added
  private fun getState() = ChatsSettingsState(
    generateLinkPreviews = SignalStore.settings().isLinkPreviewsEnabled,
    useAddressBook = SignalStore.settings().isPreferSystemContactPhotos,
    keepMutedChatsArchived = SignalStore.settings().shouldKeepMutedChatsArchived(),
    useSystemEmoji = SignalStore.settings().isPreferSystemEmoji,
    enterKeySends = SignalStore.settings().isEnterKeySends,
    chatBackupsEnabled = SignalStore.settings().isBackupEnabled,
    chatBackupsLocationApi30 = UriUtils.getFullPathFromTreeUri(ApplicationDependencies.getApplication(), SignalStore.settings().signalBackupDirectory),
    chatBackupsLocation = TextSecurePreferences.isBackupLocationRemovable(ApplicationDependencies.getApplication()),
    chatBackupZipfile = TextSecurePreferences.isRawBackupInZipfile(ApplicationDependencies.getApplication()),
    chatBackupZipfilePlain = TextSecurePreferences.isPlainBackupInZipfile(ApplicationDependencies.getApplication()),
    keepViewOnceMessages = TextSecurePreferences.isKeepViewOnceMessages(ApplicationDependencies.getApplication()),
    ignoreRemoteDelete = TextSecurePreferences.isIgnoreRemoteDelete(ApplicationDependencies.getApplication()),
    deleteMediaOnly = TextSecurePreferences.isDeleteMediaOnly(ApplicationDependencies.getApplication()),
    whoCanAddYouToGroups = TextSecurePreferences.whoCanAddYouToGroups(ApplicationDependencies.getApplication())
  )
}
