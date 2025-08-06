package com.allinone.DevView.community.repository;

import com.allinone.DevView.community.entity.CommunityPosts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityPostsRepository extends JpaRepository<CommunityPosts, Long> {

}
