package com.weihanyinian.website.module.guestbook.service;

import com.weihanyinian.website.module.guestbook.entity.Guestbook;
import com.weihanyinian.website.module.guestbook.repository.GuestbookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuestbookService {

    private final GuestbookRepository guestbookRepository;

    public GuestbookService(GuestbookRepository guestbookRepository) {
        this.guestbookRepository = guestbookRepository;
    }

    @Transactional
    public Guestbook createMessage(Guestbook message) {
        return guestbookRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Guestbook> getAllMessages() {
        return guestbookRepository.findAllByOrderByCreatedAtDesc();
    }
}
