package <%=packageName%>.web.rest;

import <%=packageName%>.domain.Resource;
import <%=packageName%>.domain.util.CustomPage;
import <%=packageName%>.domain.util.EntityWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Locale;

/**
 *
 */
public abstract class AbstractRestResource<E extends Resource<ID>, ID extends Serializable, EW extends EntityWrapper<E>> {
    @Autowired
    private MessageSource messageSource;

    protected abstract Class<E> entityClass();

    protected abstract EW entityWrapper(E entity);

    protected abstract PagingAndSortingRepository<E, ID> repository();

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomPage<E>> findAll(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                 @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
                                                 @RequestParam(value = "sort", required = false, defaultValue = "id") String sort,
                                                 @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction) throws Exception {
        CustomPage<E> customPage = new CustomPage<>(entityClass());
        customPage.setPage(repository().findAll(new PageRequest(page, size, new Sort(Sort.Direction.fromString(direction), sort))));
        return new ResponseEntity<>(customPage, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EW> findOne(@PathVariable("id") ID id) throws Exception {
        if (repository().exists(id)) {
            return new ResponseEntity<>(entityWrapper(repository().findOne(id)), HttpStatus.OK);
        } else {
            throw new EntityNotFoundException(entityClass().getSimpleName());
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EW> create(@RequestBody @Valid EW v) throws Exception {
        return new ResponseEntity<>(entityWrapper(repository().save(v.getEntity())), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EW> update(@PathVariable("id") ID id, @RequestBody @Valid EW v) throws Exception {
        E entity = v.getEntity();
        if (repository().exists(id)) {
            //Ensure the id is set
            entity.setId(id);
            return new ResponseEntity<>(entityWrapper(repository().save(entity)), HttpStatus.OK);
        } else {
            throw new EntityNotFoundException(entityClass().getSimpleName());
        }
    }

    @RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable("id") ID id) throws Exception {
        if (repository().exists(id)) {
            repository().delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new EntityNotFoundException(entityClass().getSimpleName());
        }
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestError handleException(MethodArgumentNotValidException e) {
        RestError error = new RestError(RestError.ErrorCode.INVALID);
        error.setMessage("Invalid entity");
        for (FieldError objectError : e.getBindingResult().getFieldErrors()) {
            error.getDetailMessages().add(objectError.getField() + " " + messageSource.getMessage(objectError, Locale.getDefault()));
        }
        return error;
    }

    @ResponseBody
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RestError handleException(EntityNotFoundException e) {
        RestError error = new RestError(RestError.ErrorCode.NOT_FOUND);
        if (e.getMessage() != null) {
            error.setMessage(e.getMessage() + " not found");
        }
        return error;
    }

    @ResponseBody
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RestError handleException(BindException e) {
        RestError error = new RestError(RestError.ErrorCode.INVALID);
        error.setMessage("Invalid entity");
        for (FieldError objectError : e.getBindingResult().getFieldErrors()) {
            error.getDetailMessages().add(objectError.getField() + " " + messageSource.getMessage(objectError, Locale.getDefault()));
        }
        return error;
    }

    @ResponseBody
    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public RestError handleException(UnsupportedOperationException e) {
        RestError error = new RestError(RestError.ErrorCode.METHOD_NOT_ALLOWED);
        error.setMessage(e.getMessage());
        return error;
    }
}
