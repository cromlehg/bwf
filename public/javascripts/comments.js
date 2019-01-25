var comments = (function() {

	var state = {
		api: null,
		postId: null,
		parentId: null
	};

	var $replyBox = $(
		'<div class="comment__reply">' +
			'<div class="form-group"></div>' +
			'<div class="form-group row">' +
				'<div class="comment__submit col-auto"></div>' +
				'<div class="comment__cancel col-auto"></div>' +
			'</div>' +
		'</div>'
	);
	var $textArea = $('<textarea class="form-control" id="comment__textarea" rows="3"></textarea>');
	var $submitButton = $('<button type="button" class="btn btn-outline-primary btn-block">Submit</button>');
	var $cancelButton = $('<button type="button" class="btn btn-link">Cancel</button>');

	$replyBox.find('.form-group:first-child').append($textArea);
	$replyBox.find('.comment__submit').append($submitButton);
	$replyBox.find('.comment__cancel').append($cancelButton);

	var handleCancelButtonClick = function() {
		$replyBox.detach();
		$textArea.val('');
	};

	var handleSubmitButtonClick = function() {
		var data = {
			content: $textArea.val(),
			post_id: state.postId
		};
		if (state.parentId) data.parent_id = state.parentId;
		$submitButton.attr('disabled', true);
		$textArea.val('');
		$.ajax({
			method: 'POST',
			url: state.api,
			data: JSON.stringify(data),
			contentType: 'text/json'
		}).done(function(comment) {
			var $insertPosition;
			if (state.parentId) {
				$insertPosition = $('[data-comment="' + state.parentId + '"] > .comment__body > .comment__replies');
			} else {
				$insertPosition = $('.comments__tree');
			}
			$insertPosition.prepend(comment);
			$replyBox.detach();
		}).fail(function(result) {
			console.log(result)
		}).always(function() {
			$submitButton.attr('disabled', false);
		})
	};

	var handleCreateCommentClick = function() {
		var parentId = $(this).data('target');
		var $insertPosition;
		if (parentId) {
			state.parentId = parentId;
			$insertPosition = $('[data-comment="' + parentId + '"] > .comment__body > .comment__panel');
		} else {
			state.parentId = null;
			$insertPosition = $('.comments > .comments__panel');
		}
		$insertPosition.after($replyBox);
	};

	var init = function(api, postId) {
		state.api = api;
		state.postId = postId;
		$cancelButton.click(handleCancelButtonClick);
		$submitButton.click(handleSubmitButtonClick);
		$(document).on('click', '[data-action="reply"]', handleCreateCommentClick)
	};

	return {
		init: init
	}

})();
