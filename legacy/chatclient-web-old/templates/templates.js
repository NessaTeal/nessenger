// This file was automatically generated from templates.soy.
// Please don't edit this file by hand.

/**
 * @fileoverview Templates in namespace templates.
 */

if (typeof templates == 'undefined') { var templates = {}; }


templates.message = function(opt_data, opt_ignored) {
  return '<div class="list-group-item list-group-item-action flex-column align-items-start"><div class="d-flex w-100 justify-content-between"><h5 class="mb-1">' + soy.$$escapeHtml(opt_data.origin) + '</h5><small>' + soy.$$escapeHtml(opt_data.date) + '</small></div><p class="mb-1">' + soy.$$escapeHtml(opt_data.message) + '</p></div>';
};
if (goog.DEBUG) {
  templates.message.soyTemplateName = 'templates.message';
}


templates.chatroom = function(opt_data, opt_ignored) {
  return '<a class="list-group-item list-group-item-action flex-column align-items-start" id="chatroom' + soy.$$escapeHtml(opt_data.chatroomID) + '" chatroomName="' + soy.$$escapeHtml(opt_data.chatroomName) + '"><div class="d-flex w-100 justify-content-between"><h5 class="mb-1">' + soy.$$escapeHtml(opt_data.chatroomName) + '</h5><small>' + soy.$$escapeHtml(opt_data.chatroomSize) + '</small></div></a>';
};
if (goog.DEBUG) {
  templates.chatroom.soyTemplateName = 'templates.chatroom';
}
