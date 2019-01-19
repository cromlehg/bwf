  function uploadPage(query, page, call, callback) {
	call().ajax({
	  data: JSON.stringify({page_id: page}),
	  contentType: 'text/json'
	}).done(data => {
	  $(query).html(data);
	  if(callback != null) callback();
    }).fail(data => {
	  alert(data.responseText);
    });
  }

  function pagesCount(call, callback) {
	call().ajax({
	  data: JSON.stringify({}),
	  contentType: 'text/json'
	}).done(data => {
	  callback(data);
	}).fail(data => {
	  alert(data.responseText);
	});
  }

  function updatePaginationControls(ctrlSel, pageId, pagesCount, pagesCountCall, pageCall, tableSelector) {
	$(ctrlSel).hide();
	$(ctrlSel + " > .pagination-control-next").hide();
	$(ctrlSel + " > .pagination-control-prev").hide();
	$(ctrlSel + " > .pagination-control-next").unbind("click");
	$(ctrlSel + " > .pagination-control-prev").unbind("click");

	if(pagesCount > 1) {

	  if(pageId < pagesCount) {
	    $(ctrlSel + " > .pagination-control-next").show();
	    $(ctrlSel + " > .pagination-control-next").click(function() {
	      page(pagesCountCall, pageCall, tableSelector, ctrlSel, pageId + 1)
	    });
	  } else {
		$(ctrlSel + " > .pagination-control-next").hide();
	  }

	  if(pageId > 1) {
	    $(ctrlSel + " > .pagination-control-prev").show();
	    $(ctrlSel + " > .pagination-control-prev").click(function() {
	      page(pagesCountCall, pageCall, tableSelector, ctrlSel, pageId - 1)
	    });
	  } else {
        $(ctrlSel + " > .pagination-control-prev").hide();
	  }

	  $(ctrlSel).show();
	}
  }

  function page(pagesCountCall, pageCall, tableSelector, ctrlSel, pageId) {
	pagesCount(pagesCountCall, function(count) {
	  if(count > 0) {
	    uploadPage(tableSelector, pageId, pageCall, function(){
          updatePaginationControls(ctrlSel, pageId, count, pagesCountCall, pageCall, tableSelector);
	    });
	  } else {
		updatePaginationControls(ctrlSel, pageId, count, pagesCountCall, pageCall, tableSelector);
	  }
    });
  }
