package github.ag777.util.software.everything;

import com.sun.jna.Library;
import com.sun.jna.WString;

import java.nio.Buffer;

/**
 * everything 底层接口
 * <a href="https://www.voidtools.com/zh-cn/support/everything/sdk/">官方sdk文档</a>
 * @author ag777 <837915770@vip.qq.com>
 * @version 2022/10/8 17:23
 */
public interface EverythingDll extends Library {

  /**
   * 设置Everything的搜索字符串。
   * @param lpSearchString 要设置的搜索字符串，使用宽字符格式。
   * @return 如果设置成功返回EVERYTHING_OK，否则返回相应的错误代码。
   */
  int Everything_SetSearchW(WString lpSearchString);

  /**
   * 设置是否启用路径匹配搜索。
   * @param bEnable 如果为true，则启用路径匹配搜索；如果为false，则禁用。
   */
  void Everything_SetMatchPath(boolean bEnable);

  /**
   * 设置是否启用大小写敏感搜索。
   * @param bEnable 如果为true，则启用大小写敏感搜索；如果为false，则禁用。
   */
  void Everything_SetMatchCase(boolean bEnable);

  /**
   * 设置是否启用全词匹配搜索。
   * @param bEnable 如果为true，则启用全词匹配搜索；如果为false，则禁用。
   */
  void Everything_SetMatchWholeWord(boolean bEnable);

  /**
   * 设置是否启用正则表达式搜索。
   * @param bEnable 如果为true，则启用正则表达式搜索；如果为false，则禁用。
   */
  void Everything_SetRegex(boolean bEnable);

  /**
   * 设置搜索结果的最大数量。
   * @param dwMax 要设置的最大结果数量。
   */
  void Everything_SetMax(int dwMax);

  /**
   * 设置搜索结果的偏移量。
   * @param dwOffset 要设置的结果偏移量。
   */
  void Everything_SetOffset(int dwOffset);

//    /**
//     * The Everything_SetReplyWindow function sets the window that will receive the the IPC Query results.
//     * <p>This function must be called before calling Everything_Query with bWait set to FALSE.
//     * Check for results with the specified window using Everything_IsQueryReply.
//     * Call Everything_SetReplyID with a unique identifier to specify multiple searches.
//     * @param hWnd The handle to the window that will receive the IPC Query reply.
//     */
//    void Everything_SetReplyWindow(int hWnd);

   /**
    * The Everything_SetReplyID function sets the unique number to identify the next query.
    * @param nId The unique number to identify the next query.
    */
   void Everything_SetReplyID(int nId);

   /**
    * The Everything_SetSort function sets how the results should be ordered.
    * @param dwSortType The sort type, can be one of the values in EverythingSorts
    */
   void Everything_SetSort(int dwSortType);

   /**
    * The Everything_SetRequestFlags function sets the desired result data.
    * <p>Make sure you include EVERYTHING_REQUEST_FILE_NAME and EVERYTHING_REQUEST_PATH if you want the result file name information returned.
    * The default request flags are EVERYTHING_REQUEST_FILE_NAME | EVERYTHING_REQUEST_PATH (0x00000003).
    * When the default flags (EVERYTHING_REQUEST_FILE_NAME | EVERYTHING_REQUEST_PATH) are used the SDK will use the old version 1 query.
    * When any other flags are used the new version 2 query will be tried first, and then fall back to version 1 query.
    * It is possible the requested data is not available, in which case after you have received your results you should call Everything_GetResultListRequestFlags to determine the available result data.
    * This function must be called before Everything_Query.
    * @param dwRequestFlags he request flags, can be zero or more of the flags in DwRequestFlags:
    */
   void Everything_SetRequestFlags(int dwRequestFlags);

//    boolean EVERYTHINGAPI Everything_IsQueryReply(UINT message, WPARAM wParam,LPARAM lParam, DWORD nI);

   /**
    * The Everything_GetNumFileResults function returns the number of visible file results.
    * @return Returns the number of visible file results.
    * If the function fails the return value is 0. To get extended error information, call Everything_GetLastError.
    */
   int Everything_GetNumFileResults();

  /**
   * 获取文件夹结果的数量。
   * 此函数返回Everything搜索结果中文件夹的数量。
   *
   * @return 返回文件夹结果的数量，如果发生错误则返回-1。
   */
  int Everything_GetNumFolderResults();

  /**
   * 获取结果的总数。
   * 此函数返回Everything搜索结果中的总条目数，包括文件和文件夹。
   *
   * @return 返回结果的总数，如果发生错误则返回-1。
   */
  int Everything_GetNumResults();

  /**
   * 获取文件结果的总数。
   * 此函数返回Everything搜索结果中文件的数量。
   *
   * @return 返回文件结果的数量，如果发生错误则返回-1。
   */
  int Everything_GetTotFileResults();

  /**
   * 获取文件夹结果的总数。
   * 此函数返回Everything搜索结果中文件夹的总数。
   *
   * @return 返回文件夹结果的总数，如果发生错误则返回-1。
   */
  int Everything_GetTotFolderResults();

  /**
   * 获取总结果的数量。
   * 此函数返回Everything搜索结果中的总条目数，包括文件和文件夹。
   *
   * @return 返回总结果的数量，如果发生错误则返回-1。
   */
  int Everything_GetTotResults();

  /**
   * 检查指定索引是否为卷结果。
   * 此函数用于判断搜索结果列表中指定索引的条目是否是一个卷。
   *
   * @param nIndex 要检查的索引值。
   * @return 如果是卷结果则返回true，否则返回false。
   */
  boolean Everything_IsVolumeResult(int nIndex);

  /**
   * 检查指定索引是否为文件夹结果。
   * 此函数用于判断搜索结果列表中指定索引的条目是否是一个文件夹。
   *
   * @param nIndex 要检查的索引值。
   * @return 如果是文件夹结果则返回true，否则返回false。
   */
  boolean Everything_IsFolderResult(int nIndex);

  /**
   * 检查指定索引是否为文件结果。
   * 此函数用于判断搜索结果列表中指定索引的条目是否是一个文件。
   *
   * @param nIndex 要检查的索引值。
   * @return 如果是文件结果则返回true，否则返回false。
   */
  boolean Everything_IsFileResult(int nIndex);

//    LPCTSTR Everything_GetResultFileName(int index);

    /**
     * The Everything_GetResultFileName function retrieves the file name part only of the visible result.
     * @param index Zero based index of the visible result.
     * @return The function returns a pointer to a null terminated string of TCHARs.
     * If the function fails the return value is NULL. To get extended error information, call Everything_GetLastError.
     */
    Buffer Everything_GetResultPath(int index);

    /**
     * The Everything_GetResultFullPathName function retrieves the full path and file name of the visible result.
     * @param nIndex Zero based index of the visible result.
     * @param lpString Pointer to the buffer that will receive the text. If the string is as long or longer than the buffer, the string is truncated and terminated with a NULL character.
     * @param nMaxCount Specifies the maximum number of characters to copy to the buffer, including the NULL character. If the text exceeds this limit, it is truncated.
     * @return <p>If lpString is NULL, the return value is the number of TCHARs excluding the null terminator needed to store the full path and file name of the visible result.
     * If lpString is not NULL, the return value is the number of TCHARs excluding the null terminator copied into lpString.
     * If the function fails the return value is 0. To get extended error information, call Everything_GetLastError.
     */
    int Everything_GetResultFullPathNameW(int nIndex, Buffer lpString, int nMaxCount);

    /**
     * The Everything_GetResultListSort function returns the actual sort order for the results.
     * @return sort types:
     */
    int Everything_GetResultListSort();

    WString Everything_GetSearchW();

    boolean Everything_GetMatchPath();

    boolean Everything_GetMatchCase();

    boolean Everything_GetMatchWholeWord();

    boolean Everything_GetRegex();

    int Everything_GetMax();

    int Everything_GetOffset();

//    HWND Everything_GetReplyWindow();

    /**
     * The Everything_GetReplyID function returns the current reply identifier for the IPC query reply.
     * @return The default reply identifier is 0.
     */
    int Everything_GetReplyID();

    int Everything_GetLastError();

    /**
     * The Everything_GetSort function returns the desired sort order for the results.
     * <p>The default sort is EVERYTHING_SORT_NAME_ASCENDING (1)
     * @return sort
     */
    int Everything_GetSort();

    /**
     * The Everything_GetRequestFlags function returns the desired result data flags.
     * @return flag
     */
    int Everything_GetRequestFlags();

    /**
     * The Everything_Query function executes an Everything IPC query with the current search state.
     * @param bWait Should the function wait for the results or return immediately.
     * Set this to FALSE to post the IPC Query and return immediately.
     * Set this to TRUE to send the IPC Query and wait for the results.
     * @return If the function succeeds, the return value is TRUE.
     *
     * If the function fails, the return value is FALSE. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_QueryW(boolean bWait);

    /**
     * The Everything_SortResultsByPath function sorts the current results by path, then file name.
     * SortResultsByPath is CPU Intensive. Sorting by path can take several seconds.
     * <p>The default result list contains no results.
     * Call Everything_Query to retrieve the result list prior to a call to Everything_SortResultsByPath.
     * For improved performance, use [Everything/SDK/Everything_SetSort|Everything_SetSort]]
     */
    void Everything_SortResultsByPath();

    /**
     * The Everything_GetResultListRequestFlags function returns the flags of available result data.
     * @return request flags:
     */
    int Everything_GetResultListRequestFlags();

    /**
     * The Everything_GetResultExtension function retrieves the extension part of a visible result.
     * @param dwIndex Zero based index of the visible result.
     * @return The function returns a pointer to a null terminated string of TCHARs.
     * If the function fails the return value is NULL. To get extended error information, call Everything_GetLastError.
     */
    Buffer Everything_GetResultExtension(int dwIndex);

    /**
     * The Everything_GetResultSize function retrieves the size of a visible result.
     * @param dwIndex Zero based index of the visible result.
     * @param lpSize Pointer to a LARGE_INTEGER to hold the size of the result.
     * @return The function returns non-zero if successful.
     * The function returns 0 if size information is unavailable. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_GetResultSize(int dwIndex, long lpSize);

//    boolean Everything_GetResultDateCreated(DWORD dwIndex, FILETIME *lpDateCreated);

//    boolean Everything_GetResultDateModified(int dwIndex, FILETIME *lpDateModified);

//    boolean Everything_GetResultDateAccessed(int dwIndex, FILETIME *lpDateModified);

    /**
     * The Everything_GetResultAttributes function retrieves the attributes of a visible result.
     * @param dwIndex Zero based index of the visible result.
     * @return The function returns zero or more of FILE_ATTRIBUTE_* flags.
     * The function returns INVALID_FILE_ATTRIBUTES if attribute information is unavailable. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetResultAttributes(int dwIndex);

    /**
     * The Everything_GetResultFileListFileName function retrieves the file list full path and filename of the visible result.
     * @param dwIndex Zero based index of the visible result.
     * @return The function returns a pointer to a null terminated string of TCHARs.
     * If the function fails the return value is NULL. To get extended error information, call Everything_GetLastError.
     * If the result specified by dwIndex is not in a file list, then the filename returned is an empty string.
     */
    Buffer Everything_GetResultFileListFileName(int dwIndex);

    /**
     * The Everything_GetResultRunCount function retrieves the number of times a visible result has been run from Everything.
     * @param dwIndex Zero based index of the visible result.
     * @return The function returns the number of times the result has been run from Everything.
     * The function returns 0 if the run count information is unavailable. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetResultRunCount(int dwIndex);

//    boolean Everything_GetResultDateRun(int dwIndex, FILETIME *lpDateRun);

//    boolean Everything_GetResultDateRecentlyChanged(int dwIndex, FILETIME *lpDateRecentlyChanged);

    /**
     * The Everything_GetResultHighlightedFileName function retrieves the highlighted file name part of the visible result.
     * @param index Zero based index of the visible result.
     * @return The function returns a pointer to a null terminated string of TCHARs.
     * If the function fails the return value is NULL. To get extended error information, call Everything_GetLastError.
     */
    Buffer Everything_GetResultHighlightedFileName(int index);

    /**
     * The Everything_GetResultHighlightedPath function retrieves the highlighted path part of the visible result.
     * @param index Zero based index of the visible result.
     * @return The function returns a pointer to a null terminated string of TCHARs.
     * If the function fails the return value is NULL. To get extended error information, call Everything_GetLastError.
     */
    Buffer Everything_GetResultHighlightedPath(int index);

    /**
     * The Everything_GetResultHighlightedFullPathAndFileName function retrieves the highlighted full path and file name of the visible result.
     * @param index Zero based index of the visible result.
     * @return The function returns a pointer to a null terminated string of TCHARs.
     * If the function fails the return value is NULL. To get extended error information, call Everything_GetLastError.
     */
    Buffer Everything_GetResultHighlightedFullPathAndFileName(int index);

    /**
     * The Everything_Reset function resets the result list and search state to the default state, freeing any allocated memory by the library.
     * <p>Calling Everything_SetSearch frees the old search and allocates the new search string.
     * Calling Everything_Query frees the old result list and allocates the new result list.
     * Calling Everything_Reset frees the current search and current result list.
     * The default state:
     */
    void Everything_Reset();

    /**
     * The Everything_CleanUp function resets the result list and search state, freeing any allocated memory by the library.
     * <p>You should call Everything_CleanUp to free any memory allocated by the Everything SDK.
     * Calling Everything_SetSearch frees the old search and allocates the new search string.
     * Calling Everything_Query frees the old result list and allocates the new result list.
     */
    void Everything_CleanUp();

    /**
     * The Everything_GetMajorVersion function retrieves the major version number of Everything.
     * @return The function returns the major version number.
     * The function returns 0 if major version information is unavailable. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetMajorVersion();

    /**
     * The Everything_GetMinorVersion function retrieves the minor version number of Everything.
     * @return The function returns the minor version number.
     * The function returns 0 if minor version information is unavailable. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetMinorVersion();

    /**
     * The Everything_GetRevision function retrieves the revision number of Everything.
     * @return The function returns the revision number.
     * The function returns 0 if revision information is unavailable. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetRevision();

    /**
     * The Everything_GetBuildNumber function retrieves the build number of Everything.
     * @return The function returns the build number.
     * The function returns 0 if build information is unavailable. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetBuildNumber();

    /**
     * The Everything_Exit function requests Everything to exit.
     * @return The function returns non-zero if the exit request was successful.
     * The function returns 0 if the request failed. To get extended error information, call Everything_GetLastError
     */
    int Everything_Exit();

    /**
     *
     * @return The function returns non-zero if the Everything database is fully loaded.
     * The function returns 0 if the database has not fully loaded or if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_IsDBLoaded();

    /**
     * The Everything_IsAdmin function checks if Everything is running as administrator or as a standard user.
     * @return The function returns non-zero if the Everything is running as an administrator.
     * The function returns 0 Everything is running as a standard user or if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_IsAdmin();

    /**
     * The Everything_IsAppData function checks if Everything is saving settings and data to %APPDATA%\Everything or to the same location as the Everything.exe.
     * @return The function returns non-zero if settings and data are saved in %APPDATA%\Everything.
     * The function returns 0 if settings and data are saved to the same location as the Everything.exe or if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_IsAppData();

    /**
     *
     * @return The function returns non-zero if the request to forcefully rebuild the Everything index was successful.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_RebuildDB();

    /**
     *
     * @return The function returns non-zero if the request to rescan all folder indexes was successful.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_UpdateAllFolderIndexes();

    /**
     * The Everything_SaveDB function requests Everything to save the index to disk.
     * @return The function returns non-zero if the request to save the Everything index to disk was successful.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_SaveDB();

    /**
     * The Everything_SaveRunHistory function requests Everything to save the run history to disk.
     * @return The function returns non-zero if the request to save the run history to disk was successful.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_SaveRunHistory();

    /**
     * The Everything_DeleteRunHistory function deletes all run history.
     * @return The function returns non-zero if run history is cleared.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_DeleteRunHistory();

    /**
     * The Everything_GetTargetMachine function retrieves the target machine of Everything.
     * @return he function returns one of the values in EverythingTargetMachines:
     */
    int Everything_GetTargetMachine();

    /**
     * The Everything_GetRunCountFromFileName function gets the run count from a specified file in the Everything index by file name.
     * @param lpFileName Pointer to a null-terminated string that specifies a fully qualified file name in the Everything index.
     * @return The function returns the number of times the file has been run from Everything.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    int Everything_GetRunCountFromFileName(Buffer lpFileName);

    /**
     * The Everything_SetRunCountFromFileName function sets the run count for a specified file in the Everything index by file name.
     * @param lpFileName Pointer to a null-terminated string that specifies a fully qualified file name in the Everything index.
     * @param dwRunCount The new run count.
     * @return The function returns non-zero if successful.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    boolean Everything_SetRunCountFromFileName(Buffer lpFileName, int dwRunCount);

    /**
     * The Everything_IncRunCountFromFileName function increments the run count by one for a specified file in the Everything by file name.
     * @param lpFileName Pointer to a null-terminated string that specifies a fully qualified file name in the Everything index.
     * @return The function returns the new run count for the specifed file.
     * The function returns 0 if an error occurred. To get extended error information, call Everything_GetLastError
     */
    int Everything_IncRunCountFromFileName(Buffer lpFileName);
}
